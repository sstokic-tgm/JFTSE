package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.core.packet.packets.authserver.*;
import com.jftse.emulator.server.core.service.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BasicAuthHandler {
    private final AuthenticationService authenticationService;

    public BasicAuthHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() == null)
            return;

        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
        }

        connection.setClient(null);
    }
}
