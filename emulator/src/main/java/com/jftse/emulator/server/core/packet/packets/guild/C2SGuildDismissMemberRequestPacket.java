package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildDismissMemberRequestPacket extends Packet {
    private int playerPositionInGuild;

    public C2SGuildDismissMemberRequestPacket(Packet packet) {
        super(packet);

        this.playerPositionInGuild = this.readInt();
    }
}