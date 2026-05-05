package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.PlayerStatistic;

public record PlayerStatisticView(long id, int basicRecordWin, int basicRecordLoss, int battleRecordWin,
                                  int battleRecordLoss, int guardianRecordWin, int guardianRecordLoss, int basicRP,
                                  int battleRP, int guardianRP, int consecutiveWins, int maxConsecutiveWins,
                                  int numberOfDisconnects, int serviceAce, int returnAce, int stroke, int slice,
                                  int lob, int smash, int volley, int topSpin, int rising, int serve,
                                  int guardBreakShot, int chargeShot, int skillShot, int totalGames) {
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
                statistic.getServiceAce(),
                statistic.getReturnAce(),
                statistic.getStroke(),
                statistic.getSlice(),
                statistic.getLob(),
                statistic.getSmash(),
                statistic.getVolley(),
                statistic.getTopSpin(),
                statistic.getRising(),
                statistic.getServe(),
                statistic.getGuardBreakShot(),
                statistic.getChargeShot(),
                statistic.getSkillShot(),
                statistic.getTotalGames()
        );
    }
}
