package com.jftse.emulator.server.core.rabbit;

import com.jftse.emulator.server.core.life.match.PlayerStats;
import com.jftse.emulator.server.core.life.match.RallyResult;
import com.jftse.emulator.server.core.life.match.RallyState;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.server.core.constants.BallHitAction;
import com.jftse.server.core.shared.rabbit.messages.MatchBallSyncMessage;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(prefix = "jftse.rabbitmq", name = "enabled", havingValue = "true")
@Log4j2
@AllArgsConstructor
public class MatchRallyStatsConsumer {
    private final Map<Integer, RallyState> rallyStateMap = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Integer, PlayerStats>> playerStatsMap = new ConcurrentHashMap<>();
    private final GameSessionManager gameSessionManager;

    @RabbitListener(queues = "match-queue")
    public void receiveMessage(MatchBallSyncMessage message) {
        if (message == null
                || message.getGameSessionId() == null
                || message.getPlayerPos() == null
                || message.getHitAct() == null) {
            return;
        }

        RallyState state = rallyStateMap.computeIfAbsent(message.getGameSessionId(), k -> new RallyState());

        synchronized (state) {
            updateRallyState(state, message);
        }

        if (isPositionPlayer(message.getPlayerPos())) {
            Map<Integer, PlayerStats> playerMap = playerStatsMap.computeIfAbsent(message.getGameSessionId(), k -> new ConcurrentHashMap<>());
            PlayerStats stats = playerMap.computeIfAbsent(message.getPlayerId(), k -> new PlayerStats());

            synchronized (stats) {
                updatePlayerStats(stats, message);
            }
        }
    }

    private void updateRallyState(RallyState state, MatchBallSyncMessage message) {
        GameSession session = gameSessionManager.getGameSessionBySessionId(message.getGameSessionId());
        if (session == null) {
            return;
        }

        int playerPos = message.getPlayerPos();
        BallHitAction hitAct = message.getHitAct();

        if (hitAct == BallHitAction.SERVE || hitAct == BallHitAction.GUARDIAN_SERVE) {
            state.setServerPosition(playerPos);
            state.setLastHitterPosition(playerPos);
            state.setRallyCount(0);
            state.setLastHitAct(hitAct);
            return;
        }

        if (state.getServerPosition() == -1 && session.isBasicMode()) {
            return;
        }

        if (playerPos != state.getLastHitterPosition()) {
            state.setRallyCount(state.getRallyCount() + 1);
        }

        state.setLastHitterPosition(playerPos);
        state.setLastHitAct(hitAct);
    }

    private void updatePlayerStats(PlayerStats stats, MatchBallSyncMessage message) {
        BallHitAction hitAct = message.getHitAct();

        switch (hitAct) {
            case STROKE -> stats.setStroke(stats.getStroke() + 1);
            case SLICE -> stats.setSlice(stats.getSlice() + 1);
            case LOB -> stats.setLob(stats.getLob() + 1);
            case SMASH -> stats.setSmash(stats.getSmash() + 1);
            case VOLLEY -> stats.setVolley(stats.getVolley() + 1);
            case TOP_SPIN -> stats.setTopSpin(stats.getTopSpin() + 1);
            case RISING -> stats.setRising(stats.getRising() + 1);
            case SERVE -> stats.setServe(stats.getServe() + 1);
            case G_BREAK_SHOT -> stats.setGuardBreakShot(stats.getGuardBreakShot() + 1);
        }

        // charge shots
        if (message.getPowerLevel() != null && message.getPowerLevel() > 0 && canBeCharged(hitAct)) {
            stats.setChargeShot(stats.getChargeShot() + 1);
        }

        // skill shots
        if (hitAct.getId() >= BallHitAction.ATT_SPEC_0.getId()) {
            stats.setSkillShot(stats.getSkillShot() + 1);
        }
    }

    private boolean canBeCharged(BallHitAction hitAct) {
        return switch (hitAct) {
            case STROKE, SLICE, LOB, SMASH, VOLLEY, TOP_SPIN, RISING -> true;
            default -> false;
        };
    }

    public RallyResult onPoint(int gameSessionId, boolean winningTeamIsRed) {
        RallyState state = rallyStateMap.get(gameSessionId);
        if (state == null) {
            return RallyResult.empty();
        }

        synchronized (state) {
            GameSession session = gameSessionManager.getGameSessionBySessionId(gameSessionId);
            if (session == null) {
                state.reset();
                return RallyResult.empty();
            }

            if (!session.isBasicMode()) {
                RallyResult result = new RallyResult(false, false,
                        state.getLastHitAct(), state.getRallyCount(), state.getServerPosition(), state.getLastHitterPosition());
                state.reset();
                return result;
            }

            if (state.getServerPosition() == -1) {
                state.reset();
                return RallyResult.empty();
            }

            boolean serverTeamIsRed = isRedTeamPosition(state.getServerPosition());
            boolean serverWon = serverTeamIsRed == winningTeamIsRed;

            int rallyCount = state.getRallyCount();

            boolean serviceAce = serverWon && rallyCount == 0;
            boolean returnAce = !serverWon && rallyCount == 1 && state.getLastHitterPosition() != state.getServerPosition();

            RallyResult result = new RallyResult(serviceAce, returnAce, state.getLastHitAct(), rallyCount,
                    state.getServerPosition(), state.getLastHitterPosition());

            state.reset();
            return result;
        }
    }

    public PlayerStats getPlayerStats(int gameSessionId, int playerId) {
        Map<Integer, PlayerStats> playerMap = playerStatsMap.get(gameSessionId);
        if (playerMap == null) {
            return new PlayerStats();
        }
        return playerMap.getOrDefault(playerId, new PlayerStats());
    }

    private boolean isRedTeamPosition(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    private boolean isPositionPlayer(int playerPos) {
        return playerPos >= 0 && playerPos < 4;
    }

    public void clearSession(int gameSessionId) {
        rallyStateMap.remove(gameSessionId);
        playerStatsMap.remove(gameSessionId);
    }
}
