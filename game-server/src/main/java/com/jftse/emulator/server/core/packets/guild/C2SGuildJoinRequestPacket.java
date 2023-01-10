package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildJoinRequestPacket extends Packet {
    private int guildId;

    public C2SGuildJoinRequestPacket(Packet packet) {
        super(packet);

        this.guildId = this.readInt();
    }
}