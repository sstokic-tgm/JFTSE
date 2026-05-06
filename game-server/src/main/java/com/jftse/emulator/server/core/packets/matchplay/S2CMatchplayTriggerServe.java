package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CMatchplayTriggerServe extends Packet {
    public S2CMatchplayTriggerServe(List<ServeInfo> serveInfo) {
        super(PacketOperations.S2CMatchplayStartServe);

        this.write((short) serveInfo.size());
        for (ServeInfo playerServeInfo : serveInfo) {
            this.write(playerServeInfo.getPlayerPosition());
            this.write((float) playerServeInfo.getPlayerStartLocation().x);
            this.write((float) playerServeInfo.getPlayerStartLocation().y);
            this.write(playerServeInfo.getServeType());
        }
    }
}