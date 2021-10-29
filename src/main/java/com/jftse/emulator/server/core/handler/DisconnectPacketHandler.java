package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.S2CDisconnectAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.service.AuthenticationService;
import com.jftse.emulator.server.networking.packet.Packet;

public class DisconnectPacketHandler extends AbstractHandler {
    private final AuthenticationService authenticationService;

    public DisconnectPacketHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null)
            return;

        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            if (account.getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
                authenticationService.updateAccount(account);
            }
        }

        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);
    }
}
