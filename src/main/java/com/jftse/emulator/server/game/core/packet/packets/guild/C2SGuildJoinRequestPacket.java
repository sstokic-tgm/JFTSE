package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
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