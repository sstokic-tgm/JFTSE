package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplayTriggerServe extends Packet {
    public S2CMatchplayTriggerServe(List<ServeInfo> serveInfo) {
        super(PacketOperations.S2CMatchplayStartServe.getValueAsChar());

        this.write((short) serveInfo.size());
        for (ServeInfo playerServeInfo : serveInfo) {
            this.write(playerServeInfo.getPlayerPosition());
            this.write((float) playerServeInfo.getPlayerStartLocation().x);
            this.write((float) playerServeInfo.getPlayerStartLocation().y);
            this.write(playerServeInfo.getServeType());
        }
    }
}