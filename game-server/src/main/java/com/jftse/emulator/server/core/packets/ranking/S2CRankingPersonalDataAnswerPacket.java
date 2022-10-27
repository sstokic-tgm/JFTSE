package com.jftse.emulator.server.core.packets.ranking;

import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;

public class S2CRankingPersonalDataAnswerPacket extends Packet {
    public S2CRankingPersonalDataAnswerPacket(char result, byte gameMode, Player player, int ranking) {
        super(PacketOperations.S2CRankingPersonalDataAnswer.getValue());

        PlayerStatistic playerStatistic = player.getPlayerStatistic();

        this.write(result);

        this.write(0); // unk
        this.write((byte) 0); // unk
        this.write((byte) 0); // unk

        this.write(ranking); // ranking
        this.write((byte) 0); // unknown / not used
        this.write(Math.toIntExact(player.getId()));
        this.write(player.getName());
        this.write(player.getLevel());
        this.write(player.getExpPoints());

        if (gameMode == GameMode.BASIC) {
            this.write(playerStatistic.getBasicRecordWin());
            this.write(playerStatistic.getBasicRecordLoss());
            this.write(playerStatistic.getBasicRP());
        } else if (gameMode == GameMode.BATTLE) {
            this.write(playerStatistic.getBattleRecordWin());
            this.write(playerStatistic.getBattleRecordLoss());
            this.write(playerStatistic.getBattleRP());
        } else {
            this.write(playerStatistic.getGuardianRecordWin());
            this.write(playerStatistic.getGuardianRecordLoss());
            this.write(playerStatistic.getGuardianRP());
        }
    }
}
