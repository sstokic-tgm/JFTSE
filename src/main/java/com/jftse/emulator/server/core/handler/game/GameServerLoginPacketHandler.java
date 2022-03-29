package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.C2SGameServerLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.S2CGameServerLoginPacket;
import com.jftse.emulator.server.core.service.AuthTokenService;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.auth.AuthToken;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.Date;

public class GameServerLoginPacketHandler extends AbstractHandler {
    private C2SGameServerLoginPacket gameServerLoginPacket;

    private final PlayerService playerService;
    private final AuthenticationService authenticationService;
    private final AuthTokenService authTokenService;

    public GameServerLoginPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        authTokenService = ServiceManager.getInstance().getAuthTokenService();
    }

    @Override
    public boolean process(Packet packet) {
        gameServerLoginPacket = new C2SGameServerLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        AuthToken authToken = authTokenService.findAuthToken(gameServerLoginPacket.getToken(), gameServerLoginPacket.getTimestamp(), gameServerLoginPacket.getAccountName());
        if (authToken == null) {
            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(gameServerLoginAnswerPacket);

            AuthToken authTokenToRemove = authTokenService.findAuthToken(gameServerLoginPacket.getToken());
            if (authTokenToRemove != null) {
                authTokenService.remove(authTokenToRemove);
            }

            return;
        }

        Player player = playerService.findByIdFetched((long) gameServerLoginPacket.getPlayerId());
        if (player != null && player.getAccount() != null && player.getAccount().getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID && player.getAccount().getUsername().equals(gameServerLoginPacket.getAccountName())) {
            Client client = connection.getClient();
            Account account = player.getAccount();

            // set last login date
            account.setLastLogin(new Date());
            // mark as logged in
            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
            account = authenticationService.updateAccount(account);

            client.setAccount(account);
            client.setActivePlayer(player);
            connection.setClient(client);
            connection.setHwid(gameServerLoginPacket.getHwid());

            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(gameServerLoginAnswerPacket);

            authTokenService.remove(authToken);
        } else {
            AuthToken authTokenToRemove = authTokenService.findAuthToken(gameServerLoginPacket.getToken());
            if (authTokenToRemove != null) {
                authTokenService.remove(authTokenToRemove);
            }

            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(gameServerLoginAnswerPacket);
        }
    }
}
