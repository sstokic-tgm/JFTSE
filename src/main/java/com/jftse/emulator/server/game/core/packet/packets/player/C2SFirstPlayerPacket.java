package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SFirstPlayerPacket extends Packet {
    private byte playerType;

    public C2SFirstPlayerPacket(Packet packet) {
        super(packet);

        this.playerType = this.readByte();
    }
}
