package com.jftse.server.core.service;

import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.model.tutorial.TutorialProgress;

import java.util.List;
import java.util.Map;

public interface ItemRewardService {
    boolean disableItemReward(ChallengeProgress challengeProgress, boolean win, int successCount, int oldSuccessCount);

    List<Product> getItemRewardChallenge(Challenge challenge, ChallengeProgress challengeProgress, boolean win, int successCount, int oldSuccessCount);

    List<Product> getItemRewardTutorial(Tutorial tutorial, TutorialProgress tutorialProgress);

    int getRewardExp(boolean progressExists, int rewardExp, boolean win);

    int getRewardGold(boolean progressExists, int rewardGold, boolean win);

    List<Map<String, Object>> prepareRewardItemList(Player player, List<Product> rewardProductList);
}
