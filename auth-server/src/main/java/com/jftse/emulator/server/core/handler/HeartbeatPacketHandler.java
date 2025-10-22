package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.CMSGHeartbeat;

@PacketId(CMSGHeartbeat.PACKET_ID)
public class HeartbeatPacketHandler implements PacketHandler<FTConnection, CMSGHeartbeat> {
    @Override
    public void handle(FTConnection connection, CMSGHeartbeat packet) throws Exception {
        FTClient client = connection.getClient();
        if (client != null) {
            Account account = client.getAccount();
            if (account != null && account.getStatus() == AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                connection.close();
            }
        }
    }
}
