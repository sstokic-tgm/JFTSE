package com.ft.emulator.server.game.singleplay.challenge;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.game.inventory.InventoryImpl;
import com.ft.emulator.server.game.itemreward.ItemRewardImpl;
import com.ft.emulator.server.game.level.LevelCalculatorImpl;
import com.ft.emulator.server.game.server.packets.challenge.S2CChallengeFinishPacket;
import com.ft.emulator.server.game.server.packets.challenge.S2CChallengeProgressAnswerPacket;
import com.ft.emulator.server.shared.module.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeManagerImpl extends Service {

    private final static Logger logger = LoggerFactory.getLogger("packethandler");

    private GenericModelDao<Challenge> challengeDao;
    private GenericModelDao<ChallengeProgress> challengeProgressDao;

    public ChallengeManagerImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

        challengeDao = new GenericModelDao<>(entityManagerFactory, Challenge.class);
        challengeProgressDao = new GenericModelDao<>(entityManagerFactory, ChallengeProgress.class);
    }

    public void finishChallengeGame(Client client, boolean win) {

	long timeNeeded = client.getActiveChallengeGame().getTimeNeeded();

	Map<String, Object> filters = new HashMap<>();
	filters.put("challengeIndex", client.getActiveChallengeGame().getChallengeId());
	Challenge challenge = challengeDao.find(filters);

	ChallengeProgress challengeProgress = new ChallengeProgress();
	challengeProgress.setCharacterPlayer(client.getActiveCharacterPlayer());
	challengeProgress.setChallenge(challenge);

	filters = new HashMap<>();
	filters.put("characterPlayer", challengeProgress.getCharacterPlayer());
	filters.put("challenge", challengeProgress.getChallenge());

	ChallengeProgress challengeProgressEx = challengeProgressDao.find(filters, "characterPlayer", "challenge");

	ItemRewardImpl itemRewardImpl = new ItemRewardImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	List<Product> rewardProductList = new ArrayList<>();
	int oldSuccessCount = 0;
	int successCount;
	if(challengeProgressEx == null) {

	    successCount = win ? 1 : 0;

	    rewardProductList.addAll(itemRewardImpl.getItemRewardChallenge(challenge, challengeProgressEx, win, successCount, oldSuccessCount));

	    challengeProgress.setSuccess(successCount);
	    challengeProgress.setAttempts(1);

	    try {
		challengeProgressDao.save(challengeProgress);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
	    }
	}
	else {

	    oldSuccessCount = challengeProgressEx.getSuccess();
	    successCount = win ? challengeProgressEx.getSuccess() + 1 : oldSuccessCount;

	    rewardProductList.addAll(itemRewardImpl.getItemRewardChallenge(challenge, challengeProgressEx, win, successCount, oldSuccessCount));

	    challengeProgressEx.setSuccess(successCount);
	    challengeProgressEx.setAttempts(challengeProgressEx.getAttempts() + 1);
	}
	client.setActiveChallengeGame(null);

	List<Map<String, Object>> rewardItemList = new ArrayList<>();
	try {
	    rewardItemList.addAll(itemRewardImpl.prepareRewardItemList(client.getActiveCharacterPlayer(), rewardProductList));
	}
	catch (ValidationException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	boolean disableItemReward = itemRewardImpl.disableItemReward(challengeProgressEx, win, successCount, oldSuccessCount);
	int rewardExp = itemRewardImpl.getRewardExp(disableItemReward, challenge.getRewardExp(), win);
	int rewardGold = itemRewardImpl.getRewardGold(disableItemReward, challenge.getRewardGold(), win);

	LevelCalculatorImpl levelCalculatorImpl = new LevelCalculatorImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	byte level = levelCalculatorImpl.getLevel(rewardExp, client.getActiveCharacterPlayer().getExpPoints(), client.getActiveCharacterPlayer().getLevel());

	CharacterPlayer characterPlayer = client.getActiveCharacterPlayer();
	characterPlayer.setExpPoints(characterPlayer.getExpPoints() + rewardExp);
	characterPlayer.setGold(characterPlayer.getGold() + rewardGold);

	try {
	    characterPlayer = levelCalculatorImpl.setNewLevelStatusPoints(level, characterPlayer);
	}
	catch (ValidationException e) {

	    logger.error(e.getMessage());
	    e.printStackTrace();
	}
	if(challengeProgressEx != null) {

	    try {
		challengeProgressDao.save(challengeProgressEx);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
	    }
	}
	client.setActiveCharacterPlayer(characterPlayer);

	S2CChallengeFinishPacket challengeFinishPacket = new S2CChallengeFinishPacket(win, level, rewardExp, rewardGold, (int)Math.ceil((double)timeNeeded / 1000), rewardItemList);
	client.getPacketStream().write(challengeFinishPacket);
    }
}