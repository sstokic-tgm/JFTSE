package com.ft.emulator.server.game.itemreward;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.database.model.tutorial.Tutorial;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import com.ft.emulator.server.game.inventory.InventoryImpl;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRewardImpl extends Service {

    private GenericModelDao<CharacterPlayerPocket> characterPlayerPocketDao;

    public ItemRewardImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

	characterPlayerPocketDao = new GenericModelDao<>(entityManagerFactory, CharacterPlayerPocket.class);
    }

    public boolean disableItemReward(ChallengeProgress challengeProgress, Boolean win, Integer successCount, Integer oldSuccessCount) {
	boolean disableItemReward = false;
	if(challengeProgress == null && !win) {
	    disableItemReward = true;
	}
	else if(challengeProgress != null && win && (successCount != 0 && oldSuccessCount == 0)) {
	    disableItemReward = false;
	}
	else if(challengeProgress != null && win && (successCount != 0 && oldSuccessCount != 0)) {
	    disableItemReward = true;
	}
	else if(challengeProgress != null && !win) {
	    disableItemReward = true;
	}
	return disableItemReward;
    }

    public List<Product> getItemRewardChallenge(Challenge challenge, ChallengeProgress challengeProgress, Boolean win, Integer successCount, Integer oldSuccessCount) {

        boolean disableItemReward = disableItemReward(challengeProgress, win, successCount, oldSuccessCount);

	return getProducts(challenge.getItemRewardRepeat(), disableItemReward, challenge.getRewardItem1(), challenge.getRewardItem2(), challenge.getRewardItem3());
    }

    public List<Product> getItemRewardTutorial(Tutorial tutorial, TutorialProgress tutorialProgress) {

	return getProducts(tutorial.getItemRewardRepeat(), tutorialProgress != null, tutorial.getRewardItem1(), tutorial.getRewardItem2(), tutorial.getRewardItem3());
    }

    private List<Product> getProducts(Boolean itemRewardRepeat, Boolean disableItemReward, Integer rewardItem1, Integer rewardItem2, Integer rewardItem3) {

	if(!itemRewardRepeat && disableItemReward) {
	    return new ArrayList<>();
	}

	List<Long> rewardItemList = new ArrayList<>();
	rewardItemList.add(rewardItem1.longValue());
	rewardItemList.add(rewardItem2.longValue());
	rewardItemList.add(rewardItem3.longValue());

	List<Long> rewardItems = rewardItemList.stream()
		.filter(ril -> ril != 0)
		.collect(Collectors.toList());

	if(rewardItems.isEmpty()) {
	    return new ArrayList<>();
	}

	EntityManager em = entityManagerFactory.createEntityManager();

	List<Product> result = em.createQuery("FROM Product p WHERE p.productIndex IN :rewardItems", Product.class)
		.setParameter("rewardItems", rewardItems)
		.getResultList();

	em.close();

	return result;
    }

    public int getRewardExp(Boolean progressExists, int rewardExp, Boolean win) {

        if(progressExists || !win) {
            return 0;
	}
        return rewardExp;
    }

    public int getRewardGold(Boolean progressExists, int rewardGold, Boolean win) {

	if(progressExists || !win) {
	    return 0;
	}
	return rewardGold;
    }

    public List<Map<String, Object>> prepareRewardItemList(CharacterPlayer characterPlayer, List<Product> rewardProductList) throws ValidationException {

	InventoryImpl inventoryImpl = new InventoryImpl(entityManagerFactory);

	Map<String, Object> filters;
	List<Map<String, Object>> rewardItemList = new ArrayList<>();
	for(Product reward : rewardProductList) {

	    filters = new HashMap<>();
	    filters.put("itemIndex", reward.getItem0());
	    filters.put("pocket", characterPlayer.getPocket());

	    CharacterPlayerPocket characterPlayerPocketEx = characterPlayerPocketDao.find(filters);

	    if(characterPlayerPocketEx == null) {
		CharacterPlayerPocket characterPlayerPocket = new CharacterPlayerPocket();
		characterPlayerPocket.setCategory(reward.getCategory());
		characterPlayerPocket.setItemIndex(reward.getItem0());
		characterPlayerPocket.setUseType(reward.getUseType());
		characterPlayerPocket.setItemCount(1);
		characterPlayerPocket.setPocket(characterPlayer.getPocket());

		fillRewardItemList(rewardItemList, characterPlayerPocket);

		inventoryImpl.incrementPocketBelongings(characterPlayer.getPocket());
	    }
	    else {
		characterPlayerPocketEx.setItemCount(characterPlayerPocketEx.getItemCount() + 1);

		fillRewardItemList(rewardItemList, characterPlayerPocketEx);
	    }
	}
	return rewardItemList;
    }

    private void fillRewardItemList(List<Map<String, Object>> rewardItemList, CharacterPlayerPocket characterPlayerPocket) throws ValidationException {

        characterPlayerPocket = characterPlayerPocketDao.save(characterPlayerPocket);

	Map<String, Object> rewardItemMap = new HashMap<>();
	rewardItemMap.put("id", characterPlayerPocket.getId());
	rewardItemMap.put("category", characterPlayerPocket.getCategory());
	rewardItemMap.put("itemIndex", characterPlayerPocket.getItemIndex());
	rewardItemMap.put("useType", characterPlayerPocket.getUseType());
	rewardItemMap.put("itemCount", characterPlayerPocket.getItemCount());
	rewardItemMap.put("created", characterPlayerPocket.getCreated());

	rewardItemList.add(rewardItemMap);
    }
}