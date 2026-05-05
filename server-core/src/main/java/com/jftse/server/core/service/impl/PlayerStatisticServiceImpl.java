package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.repository.player.PlayerStatisticRepository;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.service.PlayerStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PlayerStatisticServiceImpl implements PlayerStatisticService {
    private final PlayerStatisticRepository playerStatisticRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerStatistic save(PlayerStatistic playerStatistic) {
        return playerStatisticRepository.save(playerStatistic);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerStatistic findPlayerStatisticById(Long id) {
        Optional<PlayerStatistic> playerStatistic = playerStatisticRepository.findById(id);
        return playerStatistic.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerStatistic> findAllByIdIn(List<Long> ids) {
        return playerStatisticRepository.findAllByIdIn(ids);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerStatistic updatePlayerStats(Long playerStatisticId, int gameMode, boolean isWin, int rankingPoints, int serviceAces, int returnAces,
                                             int strokes, int slices, int lobs, int smashes, int volleys, int topSpins, int risings, int serves,
                                             int guardBreakShots, int chargeShots, int skillShots) {
        PlayerStatistic playerStatistic = findPlayerStatisticById(playerStatisticId);
        if (playerStatistic == null) {
            return null;
        }

        if (isWin) {
            if (gameMode == GameMode.BASIC) {
                playerStatistic.setBasicRecordWin(playerStatistic.getBasicRecordWin() + 1);
            }

            if (gameMode == GameMode.BATTLE) {
                playerStatistic.setBattleRecordWin(playerStatistic.getBattleRecordWin() + 1);
            }

            if (gameMode == GameMode.GUARDIAN) {
                playerStatistic.setGuardianRecordWin(playerStatistic.getGuardianRecordWin() + 1);
            }

            if (gameMode != GameMode.GUARDIAN) {
                int newCurrentConsecutiveWins = playerStatistic.getConsecutiveWins() + 1;
                if (newCurrentConsecutiveWins > playerStatistic.getMaxConsecutiveWins()) {
                    playerStatistic.setMaxConsecutiveWins(newCurrentConsecutiveWins);
                }

                playerStatistic.setConsecutiveWins(newCurrentConsecutiveWins);
            }
        } else {
            if (gameMode == GameMode.BASIC) {
                playerStatistic.setBasicRecordLoss(playerStatistic.getBasicRecordLoss() + 1);
            }

            if (gameMode == GameMode.BATTLE) {
                playerStatistic.setBattleRecordLoss(playerStatistic.getBattleRecordLoss() + 1);
            }

            if (gameMode == GameMode.GUARDIAN) {
                playerStatistic.setGuardianRecordLoss(playerStatistic.getGuardianRecordLoss() + 1);
            }

            if (gameMode != GameMode.GUARDIAN) {
                playerStatistic.setConsecutiveWins(0);
            }
        }

        if (gameMode == GameMode.BASIC) {
            playerStatistic.setBasicRP(Math.max(rankingPoints, 0));
        }

        if (gameMode == GameMode.BATTLE) {
            playerStatistic.setBattleRP(Math.max(rankingPoints, 0));
        }

        if (gameMode == GameMode.GUARDIAN) {
            playerStatistic.setGuardianRP(Math.max(rankingPoints, 0));
        }

        playerStatistic.setServiceAce(playerStatistic.getServiceAce() + serviceAces);
        playerStatistic.setReturnAce(playerStatistic.getReturnAce() + returnAces);
        playerStatistic.setStroke(playerStatistic.getStroke() + strokes);
        playerStatistic.setSlice(playerStatistic.getSlice() + slices);
        playerStatistic.setLob(playerStatistic.getLob() + lobs);
        playerStatistic.setSmash(playerStatistic.getSmash() + smashes);
        playerStatistic.setVolley(playerStatistic.getVolley() + volleys);
        playerStatistic.setTopSpin(playerStatistic.getTopSpin() + topSpins);
        playerStatistic.setRising(playerStatistic.getRising() + risings);
        playerStatistic.setServe(playerStatistic.getServe() + serves);
        playerStatistic.setGuardBreakShot(playerStatistic.getGuardBreakShot() + guardBreakShots);
        playerStatistic.setChargeShot(playerStatistic.getChargeShot() + chargeShots);
        playerStatistic.setSkillShot(playerStatistic.getSkillShot() + skillShots);

        return playerStatisticRepository.save(playerStatistic);
    }
}
