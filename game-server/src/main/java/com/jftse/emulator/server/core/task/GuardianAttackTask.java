package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.service.GuardianSkillsService;
import com.jftse.server.core.thread.AbstractTask;

import java.util.ArrayList;

public class GuardianAttackTask extends AbstractTask {
    private final FTConnection connection;

    private final GuardianSkillsService guardianSkillsService;

    private final EventHandler eventHandler;

    public GuardianAttackTask(FTConnection connection) {
        this.connection = connection;

        this.guardianSkillsService = ServiceManager.getInstance().getGuardianSkillsService();

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        final ArrayList<GuardianBattleState> guardianBattleStates = new ArrayList<>(game.getGuardianBattleStates());
        guardianBattleStates.forEach(guardianBattleState -> {
            final Skill skill = guardianSkillsService.getRandomGuardianSkillBasedOnProbability(guardianBattleState.getBtItemId(), guardianBattleState.getId(), game.getScenario(), game.getMap());
            final int skillIndex = skill.getId().intValue();
            S2CMatchplayGiveSpecificSkill packet = new S2CMatchplayGiveSpecificSkill((short) 0, (short) guardianBattleState.getPosition(), skillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);
        });

        RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new GuardianAttackTask(connection), MatchplayGuardianGame.guardianAttackLoopTime);
        gameSession.getFireables().push(runnableEvent);
        eventHandler.push(runnableEvent);
    }
}
