package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.PlayerPositionInfo;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySetPlayerPosition extends Packet {
    public S2CMatchplaySetPlayerPosition(List<PlayerPositionInfo> positionInfo) {
        super(PacketID.S2CMatchplaySetPlayerPosition);

        this.write((char) positionInfo.size());
        for (PlayerPositionInfo playerPositionInfo : positionInfo) {
            this.write(playerPositionInfo.getPlayerPosition());
            this.write((float) playerPositionInfo.getPlayerStartLocation().x);
            this.write((float) playerPositionInfo.getPlayerStartLocation().y);
        }
    }
}