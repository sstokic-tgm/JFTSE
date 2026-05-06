package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.AuthTokenService;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.PlayerLoadType;
import com.jftse.server.core.shared.packets.game.CMSGLoginData;
import com.jftse.server.core.shared.packets.game.SMSGLoginData;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGLoginData.PACKET_ID)
public class GameServerLoginPacketHandler implements PacketHandler<FTConnection, CMSGLoginData> {
    private final AuthTokenService authTokenService;
    private final PlayerService playerService;
    private final AuthenticationService authenticationService;

    public GameServerLoginPacketHandler() {
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
        playerService = ServiceManager.getInstance().getPlayerService();
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public void handle(FTConnection connection, CMSGLoginData packet) {
        AuthToken authToken = authTokenService.findAuthToken(packet.getToken(), packet.getTimestamp(), packet.getAccountName());
        if (authToken == null) {
            SMSGLoginData response = SMSGLoginData.builder()
                    .result((char) -1)
                    .serverType((byte) 1)
                    .build();
            connection.sendTCP(response);

            AuthToken authTokenToRemove = authTokenService.findAuthToken(packet.getToken());
            if (authTokenToRemove != null) {
                authTokenService.remove(authTokenToRemove);
            }

            return;
        }
        FTClient client = connection.getClient();
        Player player = playerService.findWithAccountById((long) packet.getPlayerId());
        if (player != null && player.getAccount().getStatus().shortValue() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID && player.getAccount().getUsername().equals(packet.getAccountName()) && !StringUtils.isEmpty(player.getName())) {
            Account account = player.getAccount();
            account.setLastSelectedPlayerId(player.getId());
            authenticationService.updateAccount(account);

            UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                    .setAccountId(account.getId())
                    .setTimestamp(System.currentTimeMillis())
                    .setServer(ServerType.GAME_SERVER.getValue())
                    .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.LOGIN.getValue()).build())
                    .build();
            ServiceManager.getInstance().getGrpcAuthService().updateAccount(request);

            player.setOnline(true);
            player = playerService.save(player);

            client.loadPlayer(account, player, PlayerLoadType.BASIC);
            connection.setClient(client);
            connection.setHwid(packet.getHwid());

            SMSGLoginData response = SMSGLoginData.builder()
                    .result((char) 0)
                    .serverType((byte) 1)
                    .build();
            connection.sendTCP(response);

            log.info("{} connected", player.getName());

            GameEventBus.call(GameEventType.ON_LOGIN, client);
        } else {
            AuthToken authTokenToRemove = authTokenService.findAuthToken(packet.getToken());
            if (authTokenToRemove != null) {
                authTokenService.remove(authTokenToRemove);
            }

            SMSGLoginData response = SMSGLoginData.builder()
                    .result((char) -1)
                    .serverType((byte) 1)
                    .build();
            connection.sendTCP(response);
        }
    }
}
