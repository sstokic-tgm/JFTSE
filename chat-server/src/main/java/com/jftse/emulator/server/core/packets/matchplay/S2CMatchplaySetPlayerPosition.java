package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.PlayerPositionInfo;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CMatchplaySetPlayerPosition extends Packet {
    public S2CMatchplaySetPlayerPosition(List<PlayerPositionInfo> positionInfo) {
        super(PacketOperations.S2CMatchplaySetPlayerPosition);

        this.write((char) positionInfo.size());
        for (PlayerPositionInfo playerPositionInfo : positionInfo) {
            this.write(playerPositionInfo.getPlayerPosition());
            this.write((float) playerPositionInfo.getPlayerStartLocation().x);
            this.write((float) playerPositionInfo.getPlayerStartLocation().y);
        }
    }
}