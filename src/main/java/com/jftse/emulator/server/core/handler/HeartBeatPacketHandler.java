package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.networking.packet.Packet;

public class HeartBeatPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() != null && connection.getClient().getAccount() != null) {
            Account account = connection.getClient().getAccount();
            if (account.getStatus() == -6)
                connection.close();
        }
    }
}
