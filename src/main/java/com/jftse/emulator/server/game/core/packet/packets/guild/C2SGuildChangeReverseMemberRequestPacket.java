package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChangeReverseMemberRequestPacket extends Packet {
    private byte memberId;
    private byte status;
    private int playerId;

    public C2SGuildChangeReverseMemberRequestPacket(Packet packet) {
        super(packet);

        this.memberId = this.readByte();
        this.status = this.readByte();
        this.playerId = this.readInt();
    }
}