package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.AuthTokenService;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.auth.*;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@PacketId(CMSGAuthLogin.PACKET_ID)
@Log4j2
public class AuthLoginDataPacketHandler implements PacketHandler<FTConnection, CMSGAuthLogin> {
    private final AuthenticationService authenticationService;
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    public AuthLoginDataPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGAuthLogin authLoginPacket) {
        Account account = authenticationService.findAccountByUsername(authLoginPacket.getUsername());
        FTClient client = connection.getClient();
        if (client != null && account != null && account.getStatus().shortValue() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID && client.isLoginIn().compareAndSet(false, true)) {
            SMSGAuthLogin authLogin = SMSGAuthLogin.builder()
                    .result((char) 0)
                    .build();
            connection.sendTCP(authLogin);

            UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                    .setAccountId(account.getId())
                    .setTimestamp(System.currentTimeMillis())
                    .setServer(ServerType.AUTH_SERVER.getValue())
                    .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.RELOG.getValue()).build())
                    .build();

            ThreadManager.getInstance().schedule(() -> AuthenticationManager.getInstance().addUpdateAccountRequest(request), 50, TimeUnit.MILLISECONDS);

            client.prepareAccount(account);
            log.info("{} connected", account.getUsername());

            String token = StringUtils.randomString(16);
            long timestamp = Instant.now().toEpochMilli();

            AuthToken authToken = new AuthToken();
            authToken.setToken(token);
            authToken.setLoginTimestamp(timestamp);
            authToken.setAccountName(authLoginPacket.getUsername());
            authTokenService.save(authToken);

            SMSGLogin loginAnswerPacket = SMSGLogin.builder()
                    .result(AuthenticationServiceImpl.SUCCESS)
                    .token(token)
                    .timestamp(timestamp)
                    .build();
            connection.sendTCP(loginAnswerPacket);

            int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(client.getAccountId());

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

            List<GameServer> gameServerList = authenticationService.getGameServerList();
            SMSGChannelList channelList = SMSGChannelList.builder()
                    .channels(gameServerList.stream().map(gs -> com.jftse.server.core.shared.packets.auth.Channel.builder()
                            .id((byte) Math.toIntExact(gs.getId()))
                            .id2((short) Math.toIntExact(gs.getId()))
                            .type(gs.getGameServerType().getType())
                            .hostname(gs.getHost())
                            .port(gs.getPort().shortValue())
                            .population((short) 0)
                            .customChannel(gs.getIsCustomChannel())
                            .name(gs.getName())
                            .build()).toList()
                    ).build();
            connection.sendTCP(channelList);

            client.isLoginIn().set(false);
        } else {
            SMSGAuthLogin authLogin = SMSGAuthLogin.builder()
                    .result((char) -1)
                    .build();
            connection.sendTCP(authLogin);
            connection.close();
        }
    }
}
