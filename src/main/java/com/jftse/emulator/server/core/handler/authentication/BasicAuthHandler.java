package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.networking.Connection;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BasicAuthHandler {
    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() == null)
            return;

        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = connection.getClient().getAccount();
            if (account.getStatus() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
                connection.getClient().saveAccount(account);
            }
        }

        connection.setClient(null);
    }
}
