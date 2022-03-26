package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CGameServerListPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.networking.packet.Packet;

public class LoginAliveClientHandler extends AbstractHandler {
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
