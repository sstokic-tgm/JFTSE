package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.guardian.AdvancedGuardianState;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGiveSpecificSkill;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.service.GuardianSkillsService;
import com.jftse.server.core.thread.AbstractTask;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;

@Log4j2
public class GuardianAttackTask extends AbstractTask {
    private final FTConnection connection;

    private final GuardianSkillsService guardianSkillsService;

    private final GuardianBattleState guardianBattleState;

    private final EventHandler eventHandler;

    public GuardianAttackTask(FTConnection connection) {
        this.connection = connection;

        this.guardianSkillsService = ServiceManager.getInstance().getGuardianSkillsService();
        this.guardianBattleState = null;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    public GuardianAttackTask(FTConnection connection, GuardianBattleState guardianBattleState) {
        this.connection = connection;

        this.guardianSkillsService = ServiceManager.getInstance().getGuardianSkillsService();
        this.guardianBattleState = guardianBattleState;

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;
        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        final boolean hasPhaseEnded = game.isAdvancedBossGuardianMode() && !game.getPhaseManager().getIsRunning().get();
        if (this.guardianBattleState == null) {
            final ArrayList<GuardianBattleState> guardianBattleStates = new ArrayList<>(game.getGuardianBattleStates());
            guardianBattleStates.forEach(guardianBattleState -> pickAttack(gameSession, game, hasPhaseEnded, guardianBattleState));
        } else {
            final GuardianBattleState guardianBattleState = game.getGuardianBattleStates().stream()
                    .filter(gbs -> gbs.getPosition() == this.guardianBattleState.getPosition() && gbs.getCurrentHealth().get() > 0)
                    .findFirst()
                    .orElse(null);
            if (guardianBattleState == null) return;

            pickAttack(gameSession, game, hasPhaseEnded, guardianBattleState);
        }
    }

    private void pickAttack(GameSession gameSession, MatchplayGuardianGame game, boolean hasPhaseEnded, GuardianBattleState guardianBattleState) {
        final long loopTime = hasPhaseEnded || !game.isAdvancedBossGuardianMode() ? MatchplayGuardianGame.guardianAttackLoopTime : game.getPhaseManager().getGuardianAttackLoopTime((AdvancedGuardianState) guardianBattleState);
        if (loopTime != -1) {
            final Skill skill = hasPhaseEnded || !game.isAdvancedBossGuardianMode() ?
                    guardianSkillsService.getRandomGuardianSkillBasedOnProbability(guardianBattleState.getBtItemId(), guardianBattleState.getId(), guardianBattleState.isBoss(), game.getScenario(), game.getMap())
                    : guardianBattleState.getRandomGuardianSkillBasedOnProbability();
            final int skillIndex = skill.getId().intValue();

            S2CMatchplayGiveSpecificSkill packet = new S2CMatchplayGiveSpecificSkill((short) 0, (short) guardianBattleState.getPosition(), skillIndex);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);

            RunnableEvent runnableEvent = eventHandler.createRunnableEvent(new GuardianAttackTask(connection, guardianBattleState), loopTime);
            gameSession.getFireables().push(runnableEvent);
            eventHandler.push(runnableEvent);
        }
    }
}
