package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.S2CGameServerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerService;

@PacketOperationIdentifier(PacketOperations.C2SLoginAliveClient)
public class LoginAliveClientHandler extends AbstractPacketHandler {
    private final AuthenticationService authenticationService;
    private final PlayerService playerService;

    public LoginAliveClientHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Account account = client.getAccount();
        if (account == null)
            return;

        if (client.isClientAlive().compareAndSet(false, true)) {
            S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationService.getGameServerList());
            connection.sendTCP(gameServerListPacket);
        }
    }
}
