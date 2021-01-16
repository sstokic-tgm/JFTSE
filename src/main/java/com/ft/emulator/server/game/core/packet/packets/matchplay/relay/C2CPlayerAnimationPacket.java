package com.ft.emulator.server.game.core.packet.packets.matchplay.relay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class C2CPlayerAnimationPacket extends Packet {
    private List<Integer> playerIds;
    private short absoluteXPositionOnMap;
    private short absoluteYPositionOnMap;
    private short relativeXMovement;
    private short relativeYMovement;
    private byte animationType;

    public C2CPlayerAnimationPacket(Packet packet) {
        super(packet);

        this.setReadPosition(this.getReadPosition() + 10); // Unknown bytes
        this.absoluteXPositionOnMap = this.readShort();
        this.absoluteYPositionOnMap = this.readShort();
        this.relativeXMovement = this.readShort();
        this.relativeYMovement = this.readShort();
        this.animationType = this.readByte();
    }
}
