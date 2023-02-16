package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.authserver.S2CGameServerListPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;

@PacketOperationIdentifier(PacketOperations.C2SLoginAliveClient)
public class LoginAliveClientHandler extends AbstractPacketHandler {
    private final AuthenticationService authenticationService;

    public LoginAliveClientHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationService.getGameServerList());
        connection.sendTCP(gameServerListPacket);
    }
}
