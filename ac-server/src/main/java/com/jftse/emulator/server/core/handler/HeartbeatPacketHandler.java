package com.jftse.emulator.server.core.handler;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SAntiCheatHeartbeat)
public class HeartbeatPacketHandler extends AbstractPacketHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        Packet answerHeartBeat = new Packet(PacketOperations.S2CAntiCheatHeartbeat);
        answerHeartBeat.write(packet.getClientTimestamp());
        answerHeartBeat.write(System.currentTimeMillis());
        connection.sendTCP(answerHeartBeat);
    }
}
