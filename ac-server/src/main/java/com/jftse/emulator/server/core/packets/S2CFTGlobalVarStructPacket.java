package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CFTGlobalVarStructPacket extends Packet {
    public S2CFTGlobalVarStructPacket(List<Integer> propValues) {
        super(PacketOperations.S2CAntiCheatFTGlobalVarsReq);

        propValues.forEach(this::write);
    }
}
