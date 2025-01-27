package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.gameserver.C2SGameServerLoginPacket;
import com.jftse.emulator.server.core.packets.gameserver.S2CGameServerLoginPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auth.AuthToken;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthTokenService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import lombok.extern.log4j.Log4j2;

import java.util.Date;

@PacketOperationIdentifier(PacketOperations.C2SGameLoginData)
@Log4j2
public class GameServerLoginPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        client.setPlayer(gameServerLoginPacket.getPlayerId());

        Player player = client.getPlayer();
        if (player != null && player.getAccount() != null && player.getAccount().getStatus().shortValue() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID && player.getAccount().getUsername().equals(gameServerLoginPacket.getAccountName()) && !StringUtils.isEmpty(player.getName())) {
            Account account = player.getAccount();

            account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
            // set last login date
            account.setLastLogin(new Date());
            account.setLoggedInServer(ServerType.GAME_SERVER);
            account.setLastSelectedPlayerId(player.getId());
            client.saveAccount(account);

            log.info(player.getName() + " connected");

            player.setOnline(true);
            client.savePlayer(player);

            client.setAccount(account.getId());
            ((FTConnection) connection).setClient(client);
            ((FTConnection) connection).setHwid(gameServerLoginPacket.getHwid());

            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(gameServerLoginAnswerPacket);
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
