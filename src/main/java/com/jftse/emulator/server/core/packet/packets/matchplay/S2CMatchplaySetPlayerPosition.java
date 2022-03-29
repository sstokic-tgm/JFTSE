package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySetPlayerPosition extends Packet {
    public S2CMatchplaySetPlayerPosition(List<PlayerPositionInfo> positionInfo) {
        super(PacketOperations.S2CMatchplaySetPlayerPosition.getValueAsChar());

        this.write((char) positionInfo.size());
        for (PlayerPositionInfo playerPositionInfo : positionInfo) {
            this.write(playerPositionInfo.getPlayerPosition());
            this.write((float) playerPositionInfo.getPlayerStartLocation().x);
            this.write((float) playerPositionInfo.getPlayerStartLocation().y);
        }
    }
}