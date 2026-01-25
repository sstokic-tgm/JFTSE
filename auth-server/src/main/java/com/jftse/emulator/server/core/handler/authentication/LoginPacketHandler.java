package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.service.*;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.auth.CMSGLogin;
import com.jftse.server.core.shared.packets.auth.SMSGLogin;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerList;
import lombok.extern.log4j.Log4j2;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketId(CMSGLogin.PACKET_ID)
@Log4j2
public class LoginPacketHandler implements PacketHandler<FTConnection, CMSGLogin> {
    private final AuthenticationService authenticationService;
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;
    private final JdbcUtil jdbcUtil;

    public LoginPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
        jdbcUtil = AuthenticationManager.getInstance().getJdbcUtil();
    }

    @Override
    public void handle(FTConnection connection, CMSGLogin loginPacket) {
        final boolean antiCheatEnabled = AuthenticationManager.getInstance().isAntiCheatEnabled();

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        if (antiCheatEnabled && !isClientValid(inetSocketAddress, loginPacket.getHwid())) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.INVAILD_VERSION)
                    .build();
            connection.sendTCP(loginAnswer);
            return;
        }

        // version check
        if (loginPacket.getVersion() != 21108180) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.INVAILD_VERSION)
                    .build();
            connection.sendTCP(loginAnswer);
            return;
        }

        Account account = authenticationService.findAccountByUsername(loginPacket.getUsername());
        if (account == null) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.ACCOUNT_INVALID_USER_ID)
                    .build();
            connection.sendTCP(loginAnswer);
            return;
        }

        int passwordCheck = authenticationService.checkPassword(account.getPassword(), loginPacket.getPassword());
        if (passwordCheck != AuthenticationServiceImpl.SUCCESS) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result((short) passwordCheck)
                    .build();
            connection.sendTCP(loginAnswer);
            return;
        }

        Integer accountStatus = account.getStatus();

        final ConcurrentLinkedDeque<FTClient> clients = AuthenticationManager.getInstance().getClients();
        final boolean isLoggedIn = clients.stream()
                .anyMatch(client -> client.getAccountId() != null && client.getAccountId().equals(account.getId()));
        if (isLoggedIn || accountStatus.equals((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN)) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN)
                    .build();
            connection.sendTCP(loginAnswer);
            return;
        }

        if (accountStatus.equals((int) AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID)) {
            if (account.getBannedUntil() != null && account.getBannedUntil().getTime() < new Date().getTime()) {
                account.setStatus(0);
                account.setBannedUntil(null);
                account.setBanReason(null);
                accountStatus = 0;

                authenticationService.updateAccount(account);
            } else {
                SMSGLogin loginAnswer = SMSGLogin.builder()
                        .result(accountStatus.shortValue())
                        .build();
                connection.sendTCP(loginAnswer);
                return;
            }
        }

        if (isClientFlagged(loginPacket.getHwid())) {
            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID)
                    .build();
            connection.sendTCP(loginAnswer);
        } else {
            if (antiCheatEnabled && !linkAccountToClientWhitelist(inetSocketAddress, loginPacket.getHwid(), account)) {
                SMSGLogin loginAnswer = SMSGLogin.builder()
                        .result((short) -80)
                        .build();
                connection.sendTCP(loginAnswer);
                return;
            }

            FTClient client = connection.getClient();
            client.prepareAccount(account);

            UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                    .setAccountId(client.getAccountId())
                    .setTimestamp(System.currentTimeMillis())
                    .setServer(ServerType.AUTH_SERVER.getValue())
                    .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.LOGIN.getValue()).build())
                    .build();
            AuthenticationManager.getInstance().addUpdateAccountRequest(request);

            int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(client.getAccountId());
            jdbcUtil.execute(em -> {
                Query q = em.createQuery("""
                        UPDATE Player p
                            SET p.nameChangeAllowed = true
                        WHERE p.account.id = :accountId
                            AND p.alreadyCreated = true
                            AND p.nameChangeAllowed = false
                            AND EXISTS (
                                SELECT 1
                                FROM PlayerPocket pp
                                WHERE pp.pocket = p.pocket
                                    AND pp.category = :category
                                    AND pp.itemIndex = :itemIndex
                            )
                        """);
                q.setParameter("accountId", client.getAccountId());
                q.setParameter("category", EItemCategory.SPECIAL.getName());
                q.setParameter("itemIndex", 4);
                q.executeUpdate();
            });
            List<Player> playerList = playerService.getPlayerListByAccountId(client.getAccountId());

            connection.setHwid(loginPacket.getHwid());

            List<AuthToken> existingAuthTokens = authTokenService.findAuthTokensByAccountName(account.getUsername());
            if (!existingAuthTokens.isEmpty()) {
                existingAuthTokens.forEach(authTokenService::remove);
            }

            String token = StringUtils.randomString(16);
            long timestamp = Instant.now().toEpochMilli();

            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setLoginTimestamp(timestamp);
            authToken.setAccountName(account.getUsername());
            authTokenService.save(authToken);

            SMSGLogin loginAnswer = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.SUCCESS)
                    .token(token)
                    .timestamp(timestamp)
                    .build();
            connection.sendTCP(loginAnswer);

            SMSGPlayerList playerListPacket = SMSGPlayerList.builder()
                    .account(
                            com.jftse.server.core.shared.packets.auth.Account.builder()
                                    .id(Math.toIntExact(client.getAccountId()))
                                    .id2(Math.toIntExact(client.getAccountId()))
                                    .tutorialCount((byte) tutorialCount)
                                    .gameMaster(client.isGameMaster())
                                    .lastPlayedPlayerId(Math.toIntExact(client.getLastPlayedPlayerId() == null ? 0 : client.getLastPlayedPlayerId()))
                                    .build()
                    )
                    .players(playerList.stream().map(p -> com.jftse.server.core.shared.packets.auth.Player.builder()
                            .id(Math.toIntExact(p.getId()))
                            .name(p.getName())
                            .level(p.getLevel())
                            .created(p.getAlreadyCreated())
                            .canDelete(!p.getFirstPlayer())
                            .gold(p.getGold())
                            .playerType(p.getPlayerType())
                            .str(p.getStrength())
                            .sta(p.getStamina())
                            .dex(p.getDexterity())
                            .wil(p.getWillpower())
                            .statPoints(p.getStatusPoints())
                            .oldRenameAllowed(false)
                            .renameAllowed(p.getNameChangeAllowed())
                            .clothEquipment(com.jftse.server.core.shared.packets.auth.ClothEquipment.builder()
                                    .hair(p.getClothEquipment().getHair())
                                    .face(p.getClothEquipment().getFace())
                                    .dress(p.getClothEquipment().getDress())
                                    .pants(p.getClothEquipment().getPants())
                                    .socks(p.getClothEquipment().getSocks())
                                    .shoes(p.getClothEquipment().getShoes())
                                    .gloves(p.getClothEquipment().getGloves())
                                    .racket(p.getClothEquipment().getRacket())
                                    .glasses(p.getClothEquipment().getGlasses())
                                    .bag(p.getClothEquipment().getBag())
                                    .hat(p.getClothEquipment().getHat())
                                    .dye(p.getClothEquipment().getDye())
                                    .build()
                            )
                            .build()).toList()
                    ).build();
            connection.sendTCP(playerListPacket);

            String hostAddress;
            if (inetSocketAddress != null)
                hostAddress = inetSocketAddress.getAddress().getHostAddress();
            else
                hostAddress = "null";
            log.info("{} has logged in from {} with hwid {}", account.getUsername(), hostAddress, loginPacket.getHwid());
        }
    }

    private boolean isClientValid(InetSocketAddress inetSocketAddress, String hwid) {
        if (inetSocketAddress == null)
            return false;
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();

        ClientWhitelist clientWhitelist = jdbcUtil.execute(em -> {
           TypedQuery<ClientWhitelist> q = em.createQuery("SELECT cw FROM ClientWhitelist cw WHERE cw.ip = :ip AND cw.hwid = :hwid ORDER BY cw.created DESC", ClientWhitelist.class);
           q.setParameter("ip", hostAddress);
           q.setParameter("hwid", hwid);
           return q.getResultStream().findFirst().orElse(null);
        });
        return clientWhitelist != null;
    }

    private boolean isClientFlagged(String hwid) {
        ClientWhitelist clientWhitelist = jdbcUtil.execute(em -> {
            TypedQuery<ClientWhitelist> q = em.createQuery("SELECT cw FROM ClientWhitelist cw WHERE cw.hwid = :hwid AND cw.flagged = true ORDER BY cw.created DESC", ClientWhitelist.class);
            q.setParameter("hwid", hwid);
            return q.getResultStream().findFirst().orElse(null);
        });
        return clientWhitelist != null;
    }

    private boolean linkAccountToClientWhitelist(InetSocketAddress inetSocketAddress, String hwid, Account account) {
        if (inetSocketAddress != null) {
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();

            return jdbcUtil.execute(em -> {
                ClientWhitelist cw = em.createQuery("SELECT cw FROM ClientWhitelist cw WHERE cw.ip = :ip AND cw.hwid = :hwid ORDER BY cw.created DESC", ClientWhitelist.class)
                        .setParameter("ip", hostAddress)
                        .setParameter("hwid", hwid)
                        .setMaxResults(1)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (cw == null) return false;

                cw.setAccount(account);
                return true;
            });
        } else
            return false;
    }
}
