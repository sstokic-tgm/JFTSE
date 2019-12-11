package com.ft.emulator.server.game.singleplay.tutorial;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.database.model.tutorial.Tutorial;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import com.ft.emulator.server.game.itemreward.ItemRewardImpl;
import com.ft.emulator.server.game.level.LevelCalculatorImpl;
import com.ft.emulator.server.game.server.packets.tutorial.S2CTutorialFinishPacket;
import com.ft.emulator.server.game.server.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.ft.emulator.server.shared.module.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorialManagerImpl extends Service {

    private final static Logger logger = LoggerFactory.getLogger("packethandler");

    private GenericModelDao<Tutorial> tutorialDao;
    private GenericModelDao<TutorialProgress> tutorialProgressDao;
    private GenericModelDao<CharacterPlayerPocket> characterPlayerPocketDao;

    public TutorialManagerImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

	tutorialDao = new GenericModelDao<>(entityManagerFactory, Tutorial.class);
	tutorialProgressDao = new GenericModelDao<>(entityManagerFactory, TutorialProgress.class);
	characterPlayerPocketDao = new GenericModelDao<>(entityManagerFactory, CharacterPlayerPocket.class);
    }

    public void finishTutorialGame(Client client) {

	long timeNeeded = client.getActiveTutorialGame().getEndTime().getTime() - client.getActiveTutorialGame().getStartTime().getTime();

	Map<String, Object> filters = new HashMap<>();
	filters.put("tutorialIndex", client.getActiveTutorialGame().getTutorialId());

	Tutorial tutorial = tutorialDao.find(filters);

	TutorialProgress tutorialProgress = new TutorialProgress();
	tutorialProgress.setCharacterPlayer(client.getActiveCharacterPlayer());
	tutorialProgress.setTutorial(tutorial);

	filters = new HashMap<>();
	filters.put("characterPlayer", tutorialProgress.getCharacterPlayer());
	filters.put("tutorial", tutorialProgress.getTutorial());

	TutorialProgress tutorialProgressEx = tutorialProgressDao.find(filters, "characterPlayer", "tutorial");

	ItemRewardImpl itemRewardImpl = new ItemRewardImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	List<Product> rewardProductList = new ArrayList<>();
	if(tutorialProgressEx == null) {

	    rewardProductList.addAll(itemRewardImpl.getItemRewardTutorial(tutorial, tutorialProgressEx));

	    tutorialProgress.setSuccess(1);
	    tutorialProgress.setAttempts(1);

	    try {

		tutorialProgressDao.save(tutorialProgress);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
	    }
	}
	else {

	    rewardProductList.addAll(itemRewardImpl.getItemRewardTutorial(tutorial, tutorialProgressEx));

	    tutorialProgressEx.setSuccess(tutorialProgressEx.getSuccess() + 1);
	    tutorialProgressEx.setAttempts(tutorialProgressEx.getAttempts() + 1);
	}
	client.setActiveTutorialGame(null);

	List<Map<String, Object>> rewardItemList = new ArrayList<>();
	try {
	    rewardItemList.addAll(itemRewardImpl.prepareRewardItemList(client.getActiveCharacterPlayer(), rewardProductList));
	}
	catch (ValidationException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	int rewardExp = itemRewardImpl.getRewardExp(tutorialProgressEx != null, tutorial.getRewardExp(), true);
	int rewardGold = itemRewardImpl.getRewardGold(tutorialProgressEx != null, tutorial.getRewardGold(), true);

	LevelCalculatorImpl levelCalculatorImpl = new LevelCalculatorImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	byte level = levelCalculatorImpl.getLevel(rewardExp, client.getActiveCharacterPlayer().getExpPoints(), client.getActiveCharacterPlayer().getLevel());

	CharacterPlayer characterPlayer = client.getActiveCharacterPlayer();
	characterPlayer.setExpPoints(characterPlayer.getExpPoints() + tutorial.getRewardExp());
	characterPlayer.setGold(characterPlayer.getGold() + tutorial.getRewardGold());

	try {
	    characterPlayer = levelCalculatorImpl.setNewLevelStatusPoints(level, characterPlayer);
	}
	catch (ValidationException e) {

	    logger.error(e.getMessage());
	    e.printStackTrace();
	}
	if(tutorialProgressEx != null) {

	    try {
		tutorialProgressDao.save(tutorialProgressEx);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
	    }
	}
	client.setActiveCharacterPlayer(characterPlayer);

	S2CTutorialFinishPacket tutorialFinishPacket = new S2CTutorialFinishPacket(true, level, rewardExp, rewardGold, (int)Math.ceil((double)timeNeeded / 1000), rewardItemList);
	client.getPacketStream().write(tutorialFinishPacket);
    }
}