package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SDeleteMessagesRequest extends Packet {
    private Byte type;
    private Byte size;
    private List<Integer> messageIds = new ArrayList<>();

    public C2SDeleteMessagesRequest(Packet packet) {
        super(packet);

        this.type = packet.readByte(); // 0 = Message, 3 == Gift
        this.size = packet.readByte();
        for (int i = 0; i < size; i++) {
            messageIds.add(packet.readInt());
        }
    }
}
