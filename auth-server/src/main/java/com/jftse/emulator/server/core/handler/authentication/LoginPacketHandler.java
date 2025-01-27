package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.C2SLoginPacket;
import com.jftse.emulator.server.core.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketOperationIdentifier(PacketOperations.C2SLoginRequest)
@Log4j2
public class LoginPacketHandler extends AbstractPacketHandler {
    private C2SLoginPacket loginPacket;

    private final AuthenticationService authenticationService;
    private final ClientWhitelistService clientWhitelistService;
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    private final ConfigService configService;

    public LoginPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();

        configService = ServiceManager.getInstance().getConfigService();
    }

    @Override
    public boolean process(Packet packet) {
        loginPacket = new C2SLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        if (configService.getValue("anticheat.enabled", false) && !isClientValid(inetSocketAddress, loginPacket.getHwid())) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.INVAILD_VERSION);
            connection.sendTCP(loginAnswerPacket);
            return;
        }

        // version check
        if (loginPacket.getVersion() != 21108180) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.INVAILD_VERSION);
            connection.sendTCP(loginAnswerPacket);
            return;
        }

        int loginResult = authenticationService.login(loginPacket.getUsername(), loginPacket.getPassword());
        Account account = authenticationService.findAccountByUsername(loginPacket.getUsername());

        if (account == null || loginResult != AuthenticationServiceImpl.SUCCESS) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket((short) loginResult);
            connection.sendTCP(loginAnswerPacket);
        } else {
            Integer accountStatus = account.getStatus();

            final ConcurrentLinkedDeque<FTClient> clients = AuthenticationManager.getInstance().getClients();
            final boolean isLoggedIn = clients.stream()
                    .anyMatch(client -> client.getAccount() != null && client.getAccount().getId().equals(account.getId()));
            if (isLoggedIn || accountStatus.equals((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN)) {
                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
                connection.sendTCP(loginAnswerPacket);
                return;
            }

            if (accountStatus.equals((int) AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID)) {
                if (account.getBannedUntil() != null && account.getBannedUntil().getTime() < new Date().getTime()) {
                    account.setStatus(0);
                    account.setBannedUntil(null);
                    account.setBanReason(null);
                    accountStatus = 0;
                } else {
                    S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(accountStatus.shortValue());
                    connection.sendTCP(loginAnswerPacket);
                    return;
                }
            }

            if (isClientFlagged(loginPacket.getHwid())) {
                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID);
                connection.sendTCP(loginAnswerPacket);
            } else {
                if (configService.getValue("anticheat.enabled", false) && !linkAccountToClientWhitelist(inetSocketAddress, loginPacket.getHwid(), account)) {
                    S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket((short) -80);
                    connection.sendTCP(loginAnswerPacket);
                    return;
                }

                // set last login date
                account.setLastLogin(new Date());
                account.setLoggedInServer(ServerType.AUTH_SERVER);
                FTClient client = (FTClient) connection.getClient();
                client.saveAccount(account);
                client.setAccount(account.getId());

                int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(account.getId());
                List<Player> playerList = playerService.findAllByAccount(account);
                for (Player p : playerList) {
                    List<PlayerPocket> ppList = playerPocketService.getPlayerPocketItems(p.getPocket());
                    final boolean nameChangeItemPresent = ppList.stream()
                            .anyMatch(pp -> pp.getCategory().equals(EItemCategory.SPECIAL.getName()) && pp.getItemIndex() == 4);
                    if (nameChangeItemPresent && !p.getNameChangeAllowed()) {
                        p.setNameChangeAllowed(true);
                        p = playerService.save(p);
                    }
                }

                ((FTConnection) connection).setHwid(loginPacket.getHwid());

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

                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(AuthenticationServiceImpl.SUCCESS, token, timestamp);
                connection.sendTCP(loginAnswerPacket);

                S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(account, playerList, tutorialCount);
                connection.sendTCP(playerListPacket);

                String hostAddress;
                if (inetSocketAddress != null)
                    hostAddress = inetSocketAddress.getAddress().getHostAddress();
                else
                    hostAddress = "null";
                log.info(account.getUsername() + " has logged in from " + hostAddress + " with hwid " + loginPacket.getHwid());
            }
        }
    }

    private boolean isClientValid(InetSocketAddress inetSocketAddress, String hwid) {
        if (inetSocketAddress == null)
            return false;
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, hwid);
        return clientWhitelist != null;
    }

    private boolean isClientFlagged(String hwid) {
        ClientWhitelist clientWhitelist = clientWhitelistService.findByHwidAndFlaggedTrue(hwid);
        return clientWhitelist != null && clientWhitelist.getFlagged();
    }

    private boolean linkAccountToClientWhitelist(InetSocketAddress inetSocketAddress, String hwid, Account account) {
        if (inetSocketAddress != null) {
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, hwid);
            if (clientWhitelist != null) {
                clientWhitelist.setAccount(account);
                clientWhitelistService.save(clientWhitelist);
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }
}
