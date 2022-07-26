package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.C2SGameServerLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.S2CGameServerLoginPacket;
import com.jftse.emulator.server.core.service.AuthTokenService;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;

import java.util.Date;

@Log4j2
public class GameServerLoginPacketHandler extends AbstractHandler {
    private C2SGameServerLoginPacket gameServerLoginPacket;

    private final AuthTokenService authTokenService;

    public GameServerLoginPacketHandler() {
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
        Client client = connection.getClient();
        client.setPlayer(gameServerLoginPacket.getPlayerId());

        Player player = client.getPlayer();
        if (player != null && player.getAccount() != null && player.getAccount().getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID && player.getAccount().getUsername().equals(gameServerLoginPacket.getAccountName())) {
            Account account = player.getAccount();

            // set last login date
            account.setLastLogin(new Date());
            // mark as logged in
            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
            client.saveAccount(account);

            log.info(player.getName() + " connected");

            client.setAccount(account.getId());
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
