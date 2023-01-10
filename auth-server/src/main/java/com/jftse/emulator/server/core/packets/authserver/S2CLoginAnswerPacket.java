package com.jftse.emulator.server.core.packets.authserver;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.nio.charset.StandardCharsets;

public class S2CLoginAnswerPacket extends Packet {
    public S2CLoginAnswerPacket(short result) {
        super(PacketOperations.S2CLoginAnswerPacket);
        this.write(result);
    }

    public S2CLoginAnswerPacket(short result, String token, long timestamp) {
        super(PacketOperations.S2CLoginAnswerPacket);
        this.write(result);
        byte[] x = token.getBytes(StandardCharsets.UTF_8);
        for (byte b : x)
            this.write(b);
        this.write(timestamp);
    }
}
