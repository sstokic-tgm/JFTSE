package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBtItem;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBtItemList;
import com.jftse.emulator.server.core.matchplay.battle.SkillDrop;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.core.service.GuardianSkillsService;
import com.jftse.emulator.server.core.thread.AbstractTask;
import com.jftse.emulator.server.networking.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GuardianAttackTask extends AbstractTask {
    private final Connection connection;

    private final GuardianSkillsService guardianSkillsService;

    private final RunnableEventHandler runnableEventHandler;

    public GuardianAttackTask(Connection connection) {
        this.connection = connection;

        this.guardianSkillsService = ServiceManager.getInstance().getGuardianSkillsService();

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        final ArrayList<GuardianBattleState> guardianBattleStates = new ArrayList<>(game.getGuardianBattleStates());
        guardianBattleStates.forEach(guardianBattleState -> {
            int skillIndex = this.getRandomGuardianSkillBasedOnProbability(guardianBattleState.getBtItemId());
            S2CMatchplayGiveSpecificSkill packet = new S2CMatchplayGiveSpecificSkill((short) 0, (short) guardianBattleState.getPosition(), skillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);
        });

        RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(new GuardianAttackTask(connection), MatchplayGuardianGame.guardianAttackLoopTime);
        gameSession.getRunnableEvents().add(runnableEvent);
    }

    private int getRandomGuardianSkillBasedOnProbability(int btItemId) {
        final GuardianBtItemList guardianBtItemList = guardianSkillsService.findGuardianBtItemListById(btItemId);
        final List<Integer> dropRatesInt = guardianBtItemList.getGuardianBtItems().stream()
                .map(GuardianBtItem::getChance)
                .collect(Collectors.toList());

        final List<SkillDrop> skillDrops = new ArrayList<>();
        int currentPercentage = 0;
        for (int i = 0; i < dropRatesInt.size(); i++) {
            int item = dropRatesInt.get(i);
            if (item != 0) {
                SkillDrop skillDrop = new SkillDrop();
                skillDrop.setId(i);
                skillDrop.setFrom(currentPercentage);
                skillDrop.setTo(currentPercentage + item);
                skillDrops.add(skillDrop);
                currentPercentage += item;
            }
        }

        final Random random = new Random();
        final int randValue = random.nextInt(101);
        final SkillDrop skillDrop = skillDrops.stream()
                .filter(x -> x.getFrom() <= randValue && x.getTo() >= randValue)
                .findFirst()
                .orElse(null);
        final GuardianBtItem guardianBtItem = guardianBtItemList.getGuardianBtItems().get(skillDrop.getId());
        return guardianBtItem.getSkillIndex();
    }
}
