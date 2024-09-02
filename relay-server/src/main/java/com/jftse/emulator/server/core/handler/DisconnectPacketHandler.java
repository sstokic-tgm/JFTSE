package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.S2CDisconnectAnswerPacket;

@PacketOperationIdentifier(PacketOperations.C2SDisconnectRequest)
public class DisconnectPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);

        connection.close();
    }
}
