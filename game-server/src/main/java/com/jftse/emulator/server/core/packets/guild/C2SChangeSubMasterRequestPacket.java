package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChangeSubMasterRequestPacket extends Packet {
    private byte status;
    private int playerId;

    public C2SChangeSubMasterRequestPacket(Packet packet) {
        super(packet);

        this.status = this.readByte();
        this.playerId = this.readInt();
    }
}