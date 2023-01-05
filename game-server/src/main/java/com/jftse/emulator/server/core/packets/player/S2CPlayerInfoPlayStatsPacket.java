package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.PlayerStatistic;

public class S2CPlayerInfoPlayStatsPacket extends Packet {
    public S2CPlayerInfoPlayStatsPacket(PlayerStatistic playerStatistic) {
        super(PacketOperations.S2CPlayerInfoPlayStatsData);

        this.write(playerStatistic.getBasicRecordWin());
        this.write(playerStatistic.getBasicRecordLoss());
        this.write(playerStatistic.getBattleRecordWin());
        this.write(playerStatistic.getBattleRecordLoss());

        this.write(playerStatistic.getConsecutiveWins());
        this.write(0); // ??
        this.write(playerStatistic.getNumberOfDisconnects());
        this.write(playerStatistic.getTotalGames());
        this.write(0); // ??
        this.write(0); // ??
    }
}
