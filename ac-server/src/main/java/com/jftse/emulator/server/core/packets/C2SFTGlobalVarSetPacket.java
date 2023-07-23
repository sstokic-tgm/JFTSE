package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;

@Getter
public class C2SFTGlobalVarSetPacket extends Packet {
    private String confName;
    private int confValue;

    public C2SFTGlobalVarSetPacket(Packet packet) {
        super(packet);

        confName = this.readString();
        confValue = this.readInt();
    }
}
