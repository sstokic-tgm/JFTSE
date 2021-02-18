package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.challenge.Challenge;
import com.jftse.emulator.server.database.model.challenge.ChallengeProgress;
import com.jftse.emulator.server.database.model.home.AccountHome;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.challenge.ChallengeProgressRepository;
import com.jftse.emulator.server.database.repository.challenge.ChallengeRepository;
import com.jftse.emulator.server.game.core.packet.packets.challenge.S2CChallengeFinishPacket;
import com.jftse.emulator.server.game.core.singleplay.challenge.ChallengeBasicGame;
import com.jftse.emulator.server.game.core.singleplay.challenge.ChallengeBattleGame;
import com.jftse.emulator.server.networking.Connection;
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
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;

    private final ItemRewardService itemRewardService;
    private final LevelService levelService;

    private final HomeService homeService;

    public List<ChallengeProgress> findAllByPlayerIdFetched(Long playerId) {
        return challengeProgressRepository.findAllByPlayerIdFetched(playerId);
    }

    public Challenge findChallengeByChallengeIndex(Integer challengeIndex) {
        Optional<Challenge> challenge = challengeRepository.findChallengeByChallengeIndex(challengeIndex);
        return challenge.orElse(null);
    }

    public void finishGame(Connection connection, boolean win) {
        long timeNeeded = connection.getClient().getActiveChallengeGame().getTimeNeeded();

        Challenge challenge = findChallengeByChallengeIndex(connection.getClient().getActiveChallengeGame().getChallengeIndex());
        ChallengeProgress challengeProgress = challengeProgressRepository.findByPlayerAndChallenge(connection.getClient().getActivePlayer(), challenge).orElse(null);

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
            challengeProgress.setPlayer(connection.getClient().getActivePlayer());
            challengeProgress.setChallenge(challenge);
            challengeProgress.setSuccess(successCount);
            challengeProgress.setAttempts(1);
        }
        else {
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

        List<Map<String, Object>> rewardItemList = new ArrayList<>(itemRewardService.prepareRewardItemList(connection.getClient().getActivePlayer(), rewardProductList));

        // add account home bonuses to exp and gold
        AccountHome accountHome = homeService.findAccountHomeByAccountId(connection.getClient().getActivePlayer().getAccount().getId());
        if (connection.getClient().getActiveChallengeGame() instanceof ChallengeBasicGame) {
            rewardExp += (rewardExp * (accountHome.getBasicBonusExp() / 100));
            rewardGold += (rewardGold * (accountHome.getBasicBonusGold() / 100));
        }
        else if (connection.getClient().getActiveChallengeGame() instanceof ChallengeBattleGame) {
            rewardExp += (rewardExp * (accountHome.getBattleBonusExp() / 100));
            rewardGold += (rewardGold * (accountHome.getBattleBonusGold() / 100));
        }

        // global exp & gold boost of *5 hardcoded :D
        rewardExp *= 5;
        rewardGold *= 5;

        byte level = levelService.getLevel(rewardExp, connection.getClient().getActivePlayer().getExpPoints(), connection.getClient().getActivePlayer().getLevel());

        Player player = connection.getClient().getActivePlayer();
        player.setExpPoints(player.getExpPoints() + rewardExp);
        player.setGold(player.getGold() + rewardGold);

        player = levelService.setNewLevelStatusPoints(level, player);

        connection.getClient().setActivePlayer(player);

        S2CChallengeFinishPacket challengeFinishPacket = new S2CChallengeFinishPacket(win, level, rewardExp, rewardGold, (int) Math.ceil((double) timeNeeded / 1000), rewardItemList);
        connection.sendTCP(challengeFinishPacket);
    }
}
