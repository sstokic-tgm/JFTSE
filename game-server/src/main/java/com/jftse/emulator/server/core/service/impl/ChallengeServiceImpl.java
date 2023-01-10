package com.jftse.emulator.server.core.service.impl;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.bonuses.BasicHouseBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.BattleHouseBonus;
import com.jftse.emulator.server.core.life.progression.bonuses.GlobalBonus;
import com.jftse.emulator.server.core.packets.challenge.S2CChallengeFinishPacket;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.challenge.ChallengeProgressRepository;
import com.jftse.entities.database.repository.challenge.ChallengeRepository;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.service.ChallengeService;
import com.jftse.server.core.service.ItemRewardService;
import com.jftse.server.core.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;

    private final ItemRewardService itemRewardService;
    private final LevelService levelService;

    @Override
    public List<ChallengeProgress> findAllByPlayerIdFetched(Long playerId) {
        return challengeProgressRepository.findAllByPlayerIdFetched(playerId);
    }

    @Override
    public Challenge findChallengeByChallengeIndex(Integer challengeIndex) {
        Optional<Challenge> challenge = challengeRepository.findChallengeByChallengeIndex(challengeIndex);
        return challenge.orElse(null);
    }

    @Override
    public void finishGame(Connection<? extends Client<?>> connection, boolean win) {
        FTClient ftClient = (FTClient) connection.getClient();
        long timeNeeded = ftClient.getActiveChallengeGame().getTimeNeeded();

        Challenge challenge = findChallengeByChallengeIndex(ftClient.getActiveChallengeGame().getChallengeIndex());
        Player player = ftClient.getPlayer();
        ChallengeProgress challengeProgress = challengeProgressRepository.findByPlayerAndChallenge(player, challenge).orElse(null);

        int rewardExp = 0;
        int rewardGold = 0;
        int oldSuccessCount = 0;
        int successCount;

        List<Product> rewardProductList = new ArrayList<>();

        if (challengeProgress == null) {
            successCount = win ? 1 : 0;

            rewardProductList.addAll(itemRewardService.getItemRewardChallenge(challenge, null, win, successCount, oldSuccessCount));

            boolean disableItemReward = itemRewardService.disableItemReward(null, win, successCount, oldSuccessCount);
            rewardGold = itemRewardService.getRewardGold(disableItemReward, challenge.getRewardGold(), win);
            rewardExp = itemRewardService.getRewardExp(disableItemReward, challenge.getRewardExp(), win);

            challengeProgress = new ChallengeProgress();
            challengeProgress.setPlayer(player);
            challengeProgress.setChallenge(challenge);
            challengeProgress.setSuccess(successCount);
            challengeProgress.setAttempts(1);
        } else {
            oldSuccessCount = challengeProgress.getSuccess();
            successCount = win ? challengeProgress.getSuccess() + 1 : oldSuccessCount;

            rewardProductList.addAll(itemRewardService.getItemRewardChallenge(challenge, challengeProgress, win, successCount, oldSuccessCount));

            boolean disableItemReward = itemRewardService.disableItemReward(challengeProgress, win, successCount, oldSuccessCount);
            rewardGold = itemRewardService.getRewardGold(disableItemReward, challenge.getRewardGold(), win);
            rewardExp = itemRewardService.getRewardExp(disableItemReward, challenge.getRewardExp(), win);

            challengeProgress.setSuccess(successCount);
            challengeProgress.setAttempts(challengeProgress.getAttempts() + 1);
        }
        challengeProgressRepository.save(challengeProgress);

        List<Map<String, Object>> rewardItemList = new ArrayList<>(itemRewardService.prepareRewardItemList(player, rewardProductList));

        // add account home bonuses to exp and gold
        ExpGoldBonus expGoldBonus = new ExpGoldBonusImpl(rewardExp, rewardGold);
        if (ftClient.getActiveChallengeGame() instanceof ChallengeBasicGame) {
            expGoldBonus = new BasicHouseBonus(expGoldBonus, ftClient.getAccountId());
        } else if (ftClient.getActiveChallengeGame() instanceof ChallengeBattleGame) {
            expGoldBonus = new BattleHouseBonus(expGoldBonus, ftClient.getAccountId());
        }

        expGoldBonus = new GlobalBonus(expGoldBonus);
        rewardExp = expGoldBonus.calculateExp();
        rewardGold = expGoldBonus.calculateGold();

        byte oldLevel = player.getLevel();
        byte level = levelService.getLevel(rewardExp, player.getExpPoints(), player.getLevel());
        if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
            player.setExpPoints(player.getExpPoints() + rewardExp);
        player.setGold(player.getGold() + rewardGold);
        player = levelService.setNewLevelStatusPoints(level, player);
        ftClient.savePlayer(player);

        S2CChallengeFinishPacket challengeFinishPacket = new S2CChallengeFinishPacket(win, level, rewardExp, rewardGold, (int) Math.ceil((double) timeNeeded / 1000), rewardItemList);
        connection.sendTCP(challengeFinishPacket);
    }
}
