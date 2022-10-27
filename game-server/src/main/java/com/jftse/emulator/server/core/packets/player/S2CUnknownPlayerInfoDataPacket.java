package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.utils.BattleUtils;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.Pocket;

public class S2CUnknownPlayerInfoDataPacket extends Packet {
    public S2CUnknownPlayerInfoDataPacket(Player player, Pocket pocket, StatusPointsAddedDto statusPointsAddedDto, PlayerStatistic playerStatistic) {
        super(PacketOperations.S2CUnknownPlayerInfoData.getValue());

        this.write(player.getName());

        this.write(0); // ??
        this.write(0); // ??
        this.write(0); // ??
        this.write(0); // ??
        this.write(0); // ??
        this.write(0); // ??

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

        this.write((byte) 0); // ??

        this.write(player.getExpPoints());
        this.write(0); // perfect(s)
        this.write(0); // guard break(s)
        this.write(BattleUtils.calculatePlayerHp(player.getLevel()));

        // status points
        this.write(player.getStrength());
        this.write(player.getStamina());
        this.write(player.getDexterity());
        this.write(player.getWillpower());
        // cloth added status points
        this.write(statusPointsAddedDto.getStrength());
        this.write(statusPointsAddedDto.getStamina());
        this.write(statusPointsAddedDto.getDexterity());
        this.write(statusPointsAddedDto.getWillpower());
        // ??
        for (int i = 5; i < 13; i++) {
            this.write((byte) 0);
        }
        // ??
        this.write((byte) 0);
        this.write((byte) 0);
        // add hp
        this.write(0);
        // cloth added status points for shop
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        //??
        this.write(0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        this.write((byte) 0);
        // ??
        for (int i = 5; i < 13; ++i) {
            this.write((byte) 0);
        }
        // ??
        for (int i = 5; i < 13; ++i) {
            this.write((byte) 0);
        }
        this.write(player.getStatusPoints());

        this.write(0); // ??
        this.write(pocket.getMaxBelongings().shortValue());
        this.write((byte) 0); // ??

        // ??
        for (int i = 0; i < 12; i++)
            this.write(0);

        this.write(player.getCouplePoints());
        this.write(0); // ??
    }
}
