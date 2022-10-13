package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.entities.database.repository.item.ProductRepository;
import com.jftse.server.core.service.ItemRewardService;
import com.jftse.server.core.service.PlayerPocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemRewardServiceImpl implements ItemRewardService {
    private final ProductRepository productRepository;
    private final PlayerPocketService playerPocketService;
    private final PocketServiceImpl pocketService;

    @Override
    public boolean disableItemReward(ChallengeProgress challengeProgress, boolean win, int successCount, int oldSuccessCount) {
        boolean disableItemReward = false;

        if (challengeProgress == null && !win)
            disableItemReward = true;
        else if (challengeProgress != null && win && (successCount != 0 && oldSuccessCount == 0))
            disableItemReward = false;
        else if (challengeProgress != null && win && successCount != 0)
            disableItemReward = true;
        else if (challengeProgress != null && !win)
            disableItemReward = true;

        return disableItemReward;
    }

    @Override
    public List<Product> getItemRewardChallenge(Challenge challenge, ChallengeProgress challengeProgress, boolean win, int successCount, int oldSuccessCount) {
        boolean disableItemReward = disableItemReward(challengeProgress, win, successCount, oldSuccessCount);
        return getProducts(challenge.getItemRewardRepeat(), disableItemReward, challenge.getRewardItem1(), challenge.getRewardItem2(), challenge.getRewardItem3());
    }

    @Override
    public List<Product> getItemRewardTutorial(Tutorial tutorial, TutorialProgress tutorialProgress) {
        return getProducts(tutorial.getItemRewardRepeat(), tutorialProgress != null, tutorial.getRewardItem1(), tutorial.getRewardItem2(), tutorial.getRewardItem3());
    }

    private List<Product> getProducts(boolean itemRewardRepeat, boolean disableItemReward, int rewardItem1, int rewardItem2, int rewardItem3) {
        if (!itemRewardRepeat && disableItemReward) {
            return new ArrayList<>();
        }

        List<Integer> rewardItemList = new ArrayList<>();
        rewardItemList.add(rewardItem1);
        rewardItemList.add(rewardItem2);
        rewardItemList.add(rewardItem3);

        List<Integer> rewardItems = rewardItemList.stream()
            .filter(ri -> ri != 0)
            .collect(Collectors.toList());

        if (rewardItems.isEmpty()) {
            return new ArrayList<>();
        }

        return productRepository.findProductsByProductIndexIn(rewardItems);
    }

    @Override
    public int getRewardExp(boolean progressExists, int rewardExp, boolean win) {
        if (progressExists || !win) {
            return 0;
        }
        return rewardExp;
    }

    @Override
    public int getRewardGold(boolean progressExists, int rewardGold, boolean win) {
        if (progressExists || !win) {
            return 0;
        }
        return rewardGold;
    }

    @Override
    public List<Map<String, Object>> prepareRewardItemList(Player player, List<Product> rewardProductList) {
        List<Map<String, Object>> rewardItemList = new ArrayList<>();

        for (Product reward : rewardProductList) {
            PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(reward.getItem0(), reward.getCategory(), player.getPocket());

            if (playerPocket == null) {
                playerPocket = new PlayerPocket();
                playerPocket.setCategory(reward.getCategory());
                playerPocket.setItemIndex(reward.getItem0());
                playerPocket.setUseType(reward.getUseType());
                playerPocket.setItemCount(reward.getUse0() == 0 ? 1 : reward.getUse0());
                playerPocket.setPocket(player.getPocket());

                fillRewardItemList(rewardItemList, playerPocket);

                pocketService.incrementPocketBelongings(player.getPocket());
            }
            else {
                playerPocket.setItemCount(playerPocket.getItemCount() + 1);

                fillRewardItemList(rewardItemList, playerPocket);
            }
        }
        return rewardItemList;
    }

    private void fillRewardItemList(List<Map<String, Object>> rewardItemList, PlayerPocket playerPocket) {
        playerPocket = playerPocketService.save(playerPocket);

        Map<String, Object> rewardItemMap = new HashMap<>();
        rewardItemMap.put("id", playerPocket.getId());
        rewardItemMap.put("category", playerPocket.getCategory());
        rewardItemMap.put("itemIndex", playerPocket.getItemIndex());
        rewardItemMap.put("useType", playerPocket.getUseType());
        rewardItemMap.put("itemCount", playerPocket.getItemCount());
        rewardItemMap.put("created", playerPocket.getCreated());

        rewardItemList.add(rewardItemMap);
    }
}
