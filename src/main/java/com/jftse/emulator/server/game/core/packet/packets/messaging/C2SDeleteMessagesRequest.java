package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SDeleteMessagesRequest extends Packet {
    private Byte unk0;
    private Byte size;
    private List<Integer> messageIds = new ArrayList<>();

    public C2SDeleteMessagesRequest(Packet packet) {
        super(packet);

        this.unk0 = packet.readByte();
        this.size = packet.readByte();
        for (int i = 0; i < size; i++) {
            messageIds.add(packet.readInt());
        }
    }
}
