package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.PlayerStatistic;

public record PlayerStatisticView(long id, int basicRecordWin, int basicRecordLoss, int battleRecordWin,
                                  int battleRecordLoss, int guardianRecordWin, int guardianRecordLoss, int basicRP,
                                  int battleRP, int guardianRP, int consecutiveWins, int maxConsecutiveWins,
                                  int numberOfDisconnects, int totalGames) {
    public static PlayerStatisticView fromEntity(PlayerStatistic statistic) {
        return new PlayerStatisticView(
                statistic.getId(),
                statistic.getBasicRecordWin(),
                statistic.getBasicRecordLoss(),
                statistic.getBattleRecordWin(),
                statistic.getBattleRecordLoss(),
                statistic.getGuardianRecordWin(),
                statistic.getGuardianRecordLoss(),
                statistic.getBasicRP(),
                statistic.getBattleRP(),
                statistic.getGuardianRP(),
                statistic.getConsecutiveWins(),
                statistic.getMaxConsecutiveWins(),
                statistic.getNumberOfDisconnects(),
                statistic.getTotalGames()
        );
    }
}
