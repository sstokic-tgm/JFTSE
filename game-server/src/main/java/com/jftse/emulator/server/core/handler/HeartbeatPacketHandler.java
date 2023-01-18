package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;

@PacketOperationIdentifier(PacketOperations.C2SHeartbeat)
public class HeartbeatPacketHandler extends AbstractPacketHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient != null) {
            Account account = ftClient.getAccount();
            if (account != null) {
                if (account.getStatus() == AuthenticationServiceImpl.SUCCESS) {
                    account.setStatus((int) AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN);
                    ftClient.saveAccount(account);
                }
                account = ftClient.getAccount();
                if (account != null && account.getStatus() == AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                    connection.close();
                }
            }

            Packet answerHeartBeat = new Packet((char) 0xFF87);
            answerHeartBeat.write(packet.getClientTimestamp());
            connection.sendTCP(answerHeartBeat);
        }
    }
}
