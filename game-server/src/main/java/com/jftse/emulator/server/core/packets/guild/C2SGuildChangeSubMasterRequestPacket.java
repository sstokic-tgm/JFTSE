package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChangeSubMasterRequestPacket extends Packet {
    private byte status;
    private int playerPositionInGuild;

    public C2SGuildChangeSubMasterRequestPacket(Packet packet) {
        super(packet);

        this.status = this.readByte();
        this.playerPositionInGuild = this.readInt();
    }
}