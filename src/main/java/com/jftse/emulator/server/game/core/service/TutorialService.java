package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.tutorial.Tutorial;
import com.jftse.emulator.server.database.model.tutorial.TutorialProgress;
import com.jftse.emulator.server.database.repository.tutorial.TutorialProgressRepository;
import com.jftse.emulator.server.database.repository.tutorial.TutorialRepository;
import com.jftse.emulator.server.game.core.packet.packets.tutorial.S2CTutorialFinishPacket;
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
public class TutorialService {
    private final TutorialRepository tutorialRepository;
    private final TutorialProgressRepository tutorialProgressRepository;

    private final ItemRewardService itemRewardService;
    private final LevelService levelService;

    public List<TutorialProgress> findAllByPlayerIdFetched(Long playerId) {
        return tutorialProgressRepository.findAllByPlayerIdFetched(playerId);
    }

    public Tutorial findByTutorialIndex(Integer tutorialIndex) {
        Optional<Tutorial> tutorial = tutorialRepository.findByTutorialIndex(tutorialIndex);
        return tutorial.orElse(null);
    }

    public void finishGame(Connection connection) {
        long timeNeeded = connection.getClient().getActiveTutorialGame().getTimeNeeded();

        Tutorial tutorial = findByTutorialIndex(connection.getClient().getActiveTutorialGame().getTutorialIndex());
        TutorialProgress tutorialProgress = tutorialProgressRepository.findByPlayerAndTutorial(connection.getClient().getActivePlayer(), tutorial).orElse(null);

        int rewardExp = 0;
        int rewardGold = 0;
        List<Product> rewardProductList = new ArrayList<>();
        if (tutorialProgress == null) {
            rewardProductList.addAll(itemRewardService.getItemRewardTutorial(tutorial, null));

            rewardGold = itemRewardService.getRewardGold(false, tutorial.getRewardGold(), true);
            rewardExp = itemRewardService.getRewardExp(false, tutorial.getRewardExp(), true);

            tutorialProgress = new TutorialProgress();
            tutorialProgress.setPlayer(connection.getClient().getActivePlayer());
            tutorialProgress.setTutorial(tutorial);
            tutorialProgress.setSuccess(1);
            tutorialProgress.setAttempts(1);

            tutorialProgressRepository.save(tutorialProgress);
        }
        else {
            rewardProductList.addAll(itemRewardService.getItemRewardTutorial(tutorial, tutorialProgress));

            rewardGold = itemRewardService.getRewardGold(true, tutorial.getRewardGold(), true);
            rewardExp = itemRewardService.getRewardExp(true, tutorial.getRewardExp(), true);

            tutorialProgress.setSuccess(tutorialProgress.getSuccess() + 1);
            tutorialProgress.setAttempts(tutorialProgress.getAttempts() + 1);
        }
        tutorialProgressRepository.save(tutorialProgress);

        List<Map<String, Object>> rewardItemList = new ArrayList<>(itemRewardService.prepareRewardItemList(connection.getClient().getActivePlayer(), rewardProductList));

        byte level = levelService.getLevel(rewardExp, connection.getClient().getActivePlayer().getExpPoints(), connection.getClient().getActivePlayer().getLevel());

        Player player = connection.getClient().getActivePlayer();
        player.setExpPoints(player.getExpPoints() + rewardExp);
        player.setGold(player.getGold() + rewardGold);

        player = levelService.setNewLevelStatusPoints(level, player);

        connection.getClient().setActivePlayer(player);

        S2CTutorialFinishPacket tutorialFinishPacket = new S2CTutorialFinishPacket(true, level, rewardExp, rewardGold, (int) Math.ceil((double) timeNeeded / 1000), rewardItemList);
        connection.sendTCP(tutorialFinishPacket);
    }
}
