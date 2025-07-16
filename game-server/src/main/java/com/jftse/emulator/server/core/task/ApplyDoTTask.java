package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayDealDamage;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.server.core.matchplay.battle.PlayerBattleState;
import com.jftse.server.core.service.SkillService;
import com.jftse.server.core.thread.AbstractTask;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class ApplyDoTTask extends AbstractTask {
    private final FTConnection connection;
    private final PlayerBattleState player;

    private final EventHandler eventHandler;
    private final SkillService skillService;

    private final int ticks;
    private final int interval;
    private final int damagePerTick;

    private static final long BURN_SKILL_ID = 64L;

    public ApplyDoTTask(FTConnection connection, PlayerBattleState player, int ticks, int interval, int damagePerTick) {
        this.connection = connection;
        this.player = player;
        this.ticks = ticks;
        this.interval = interval;
        this.damagePerTick = damagePerTick;

        this.skillService = ServiceManager.getInstance().getSkillService();
        this.eventHandler = GameManager.getInstance().getEventHandler();
    }


    @Override
    public void run() {
        final FTClient client = connection.getClient();
        if (client == null) return;

        final GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getMatchplayGame();
        if (game instanceof MatchplayGuardianGame guardianGame) {
            AtomicInteger tickCounter = new AtomicInteger(0);
            RunnableEvent event = eventHandler.createRunnableEvent(() -> applyDoT(guardianGame, tickCounter), interval);
            eventHandler.offer(event);
        }
    }

    private void applyDoT(MatchplayGuardianGame game, AtomicInteger tickCounter) {
        final PlayerBattleState playerState = game.getPlayerBattleStates().stream()
                .filter(ps -> ps.getId() == player.getId())
                .findFirst()
                .orElse(null);

        if (playerState == null || playerState.getCurrentHealth().get() <= 0) return;

        Skill skill = skillService.findSkillById(BURN_SKILL_ID);
        if (skill != null) {
            final short newHealth = (short) Math.max(0, playerState.getCurrentHealth().addAndGet(-damagePerTick));
            final S2CMatchplayDealDamage packet = new S2CMatchplayDealDamage((short) playerState.getPosition(), newHealth, skill.getTargeting().shortValue(), skill.getId().byteValue(), 0, 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(packet, connection);
        }

        if (tickCounter.incrementAndGet() < ticks) {
            log.debug("Applying DoT damage to player: {}. Tick: {}/{}", player.getId(), tickCounter.get(), ticks);
            RunnableEvent event = eventHandler.createRunnableEvent(() -> applyDoT(game, tickCounter), interval);
            eventHandler.offer(event);
        }
    }
}
