package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
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