package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
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