package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.C2SAuthLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CAuthLoginPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.networking.packet.Packet;

public class AuthLoginDataPacketHandler extends AbstractHandler {
    private C2SAuthLoginPacket authLoginPacket;

    private final AuthenticationService authenticationService;

    public AuthLoginDataPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean process(Packet packet) {
        authLoginPacket = new C2SAuthLoginPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Account account = authenticationService.findAccountByUsername(authLoginPacket.getUsername());
        if (account != null && account.getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
            account = authenticationService.updateAccount(account);

            connection.getClient().setAccount(account);

            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(authLoginAnswerPacket);

            S2CPlayerListPacket PlayerListPacket = new S2CPlayerListPacket(account, account.getPlayerList());
            connection.sendTCP(PlayerListPacket);
        } else {
            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(authLoginAnswerPacket);
            connection.close();
        }
    }
}
