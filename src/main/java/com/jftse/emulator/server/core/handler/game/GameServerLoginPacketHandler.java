package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.C2SGameServerLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.S2CGameServerLoginPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.Date;

public class GameServerLoginPacketHandler extends AbstractHandler {
    private C2SGameServerLoginPacket gameServerLoginPacket;

    private final PlayerService playerService;
    private final AuthenticationService authenticationService;

    public GameServerLoginPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean process(Packet packet) {
        gameServerLoginPacket = new C2SGameServerLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findByIdFetched((long) gameServerLoginPacket.getPlayerId());
        if (player != null && player.getAccount() != null) {
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
        } else {
            S2CGameServerLoginPacket gameServerLoginAnswerPacket = new S2CGameServerLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(gameServerLoginAnswerPacket);
        }
    }
}
