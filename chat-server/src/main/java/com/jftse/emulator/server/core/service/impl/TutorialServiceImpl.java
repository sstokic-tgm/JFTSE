package com.jftse.emulator.server.core.service.impl;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.packets.tutorial.S2CTutorialFinishPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.entities.database.repository.tutorial.TutorialProgressRepository;
import com.jftse.entities.database.repository.tutorial.TutorialRepository;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.service.ItemRewardService;
import com.jftse.server.core.service.LevelService;
import com.jftse.server.core.service.TutorialService;
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
public class TutorialServiceImpl implements TutorialService {
    private final TutorialRepository tutorialRepository;
    private final TutorialProgressRepository tutorialProgressRepository;

    private final ItemRewardService itemRewardService;
    private final LevelService levelService;

    @Override
    public List<TutorialProgress> findAllByPlayerIdFetched(Long playerId) {
        return tutorialProgressRepository.findAllByPlayerIdFetched(playerId);
    }

    @Override
    public Tutorial findByTutorialIndex(Integer tutorialIndex) {
        Optional<Tutorial> tutorial = tutorialRepository.findByTutorialIndex(tutorialIndex);
        return tutorial.orElse(null);
    }

    @Override
    public void finishGame(Connection<? extends Client<?>> connection) {
        FTClient ftClient = (FTClient) connection.getClient();
        long timeNeeded = ftClient.getActiveTutorialGame().getTimeNeeded();

        Tutorial tutorial = findByTutorialIndex(ftClient.getActiveTutorialGame().getTutorialIndex());
        Player player = ftClient.getPlayer();
        TutorialProgress tutorialProgress = tutorialProgressRepository.findByPlayerAndTutorial(player, tutorial).orElse(null);

        int rewardExp = 0;
        int rewardGold = 0;
        List<Product> rewardProductList = new ArrayList<>();
        if (tutorialProgress == null) {
            rewardProductList.addAll(itemRewardService.getItemRewardTutorial(tutorial, null));

            rewardGold = itemRewardService.getRewardGold(false, tutorial.getRewardGold(), true);
            rewardExp = itemRewardService.getRewardExp(false, tutorial.getRewardExp(), true);

            tutorialProgress = new TutorialProgress();
            tutorialProgress.setPlayer(player);
            tutorialProgress.setTutorial(tutorial);
            tutorialProgress.setSuccess(1);
            tutorialProgress.setAttempts(1);

            tutorialProgressRepository.save(tutorialProgress);
        } else {
            rewardProductList.addAll(itemRewardService.getItemRewardTutorial(tutorial, tutorialProgress));

            rewardGold = itemRewardService.getRewardGold(true, tutorial.getRewardGold(), true);
            rewardExp = itemRewardService.getRewardExp(true, tutorial.getRewardExp(), true);

            tutorialProgress.setSuccess(tutorialProgress.getSuccess() + 1);
            tutorialProgress.setAttempts(tutorialProgress.getAttempts() + 1);
        }
        tutorialProgressRepository.save(tutorialProgress);

        List<Map<String, Object>> rewardItemList = new ArrayList<>(itemRewardService.prepareRewardItemList(player, rewardProductList));

        byte oldLevel = player.getLevel();
        byte level = levelService.getLevel(rewardExp, player.getExpPoints(), player.getLevel());
        if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
            player.setExpPoints(player.getExpPoints() + rewardExp);
        player.setGold(player.getGold() + rewardGold);
        player = levelService.setNewLevelStatusPoints(level, player);
        ftClient.savePlayer(player);

        S2CTutorialFinishPacket tutorialFinishPacket = new S2CTutorialFinishPacket(true, level, rewardExp, rewardGold, (int) Math.ceil((double) timeNeeded / 1000), rewardItemList);
        connection.sendTCP(tutorialFinishPacket);
    }
}
