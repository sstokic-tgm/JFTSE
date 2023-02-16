package com.jftse.emulator.server.core.utils;

import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;

import java.util.HashMap;
import java.util.List;

public class RankingUtils {
    private static double transformedRating(int rating) {
        return Math.pow(10, (double) (rating / 400));
    }

    private static double expectedScore(Player player1, Player player2, byte gameMode) {
        double expectedScore = 0.0;
        if (gameMode == GameMode.BASIC) {
            double player1TransformedRating = transformedRating(player1.getPlayerStatistic().getBasicRP());
            double player2TransformedRating = transformedRating(player2.getPlayerStatistic().getBasicRP());
            expectedScore = player1TransformedRating / (player1TransformedRating + player2TransformedRating);
        } else if (gameMode == GameMode.BATTLE) {
            double player1TransformedRating = transformedRating(player1.getPlayerStatistic().getBattleRP());
            double player2TransformedRating = transformedRating(player2.getPlayerStatistic().getBattleRP());
            expectedScore = player1TransformedRating / (player1TransformedRating + player2TransformedRating);
        }
        return expectedScore;
    }

    private static int determineKFactor(int rating) {
        if (rating < 2000)
            return 32;
        else if (rating >= 2000 && rating < 2400)
            return 24;
        else
            return  16;
    }

    public static HashMap<Long, Integer> calculateNewRating(List<Player> playerList, Player currentPlayer, boolean won, byte gameMode) {
        HashMap<Long, Integer> result = new HashMap<>();

        double Q = 0.0;
        for (Player player : playerList) {
            if (gameMode == GameMode.BASIC) {
                Q += transformedRating(player.getPlayerStatistic().getBasicRP());
            } else if (gameMode == GameMode.BATTLE) {
                Q += transformedRating(player.getPlayerStatistic().getBattleRP());
            }
        }

        for (Player player : playerList) {

            double expected = 0.0;
            if (gameMode == GameMode.BASIC) {
                expected = transformedRating(player.getPlayerStatistic().getBasicRP()) / Q;
            } else if (gameMode == GameMode.BATTLE) {
                expected = transformedRating(player.getPlayerStatistic().getBattleRP()) / Q;
            }

            int actualScore;
            if (player.getId().equals(currentPlayer.getId()) && won)
                actualScore = 1;
            else
                actualScore = 0;

            int newRating = 0;
            if (gameMode == GameMode.BASIC) {
                int K = determineKFactor(player.getPlayerStatistic().getBasicRP());
                newRating = (int) Math.round(player.getPlayerStatistic().getBasicRP() + K * (actualScore - expected));
            } else if (gameMode == GameMode.BATTLE) {
                int K = determineKFactor(player.getPlayerStatistic().getBattleRP());
                newRating = (int) Math.round(player.getPlayerStatistic().getBattleRP() + K * (actualScore - expected));
            }

            result.put(player.getId(), newRating);
        }

        return result;
    }

    public static int calculateNewRating(Player player1, Player player2, boolean won, byte gameMode) {
        int actualScore;
        if (won)
            actualScore = 1;
        else
            actualScore = 0;

        int newRating = 0;
        if (gameMode == GameMode.BASIC) {
            int K = determineKFactor(player1.getPlayerStatistic().getBasicRP());
            newRating = (int) Math.round(player1.getPlayerStatistic().getBasicRP() + K * (actualScore - expectedScore(player1, player2, gameMode)));
        } else if (gameMode == GameMode.BATTLE) {
            int K = determineKFactor(player1.getPlayerStatistic().getBattleRP());
            newRating = (int) Math.round(player1.getPlayerStatistic().getBattleRP() + K * (actualScore - expectedScore(player1, player2, gameMode)));
        }
        return newRating;
    }
}
