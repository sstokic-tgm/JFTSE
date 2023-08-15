package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChangeMasterRequestPacket extends Packet {
    private int playerPositionInGuild;

    public C2SGuildChangeMasterRequestPacket(Packet packet) {
        super(packet);

        this.playerPositionInGuild = this.readInt();
    }
}