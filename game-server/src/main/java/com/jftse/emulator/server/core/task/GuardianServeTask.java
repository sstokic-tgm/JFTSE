package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.guardian.PhaseManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.thread.AbstractTask;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class GuardianServeTask extends AbstractTask {
    private final FTConnection connection;

    private final Random random;

    public GuardianServeTask(FTConnection connection) {
        this.connection = connection;

        random = new Random();
    }

    @Override
    public void run() {
        FTClient client = connection.getClient();
        if (client == null) return;

        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) return;
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        if (!game.getStageChangingToBoss().get()) {
            return;
        }

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, servingPositionXOffset, servingPositionYOffset);
        S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(null);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setNameColorAndRemoveBlackBarPacket, connection);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, connection);
        game.resetStageStartTime();
        ThreadManager.getInstance().newTask(new DefeatTimerTask(connection, gameSession));
        ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));

        if (game.isAdvancedBossGuardianMode()) {
            final PhaseManager phaseManager = game.getPhaseManager();
            phaseManager.start();
            phaseManager.getIsRunning().set(true);
            Future<?> updateTask = ThreadManager.getInstance().scheduleAtFixedRate(() -> {
                if (!phaseManager.getIsRunning().get() || phaseManager.getIsChangingPhase().get() || phaseManager.getIsPhaseEnding().get())
                    return;

                try {
                    phaseManager.update(connection);
                } catch (Exception e) {
                    log.error("updateTask exception", e);
                }
            }, 1, TimeUnit.SECONDS);
            game.getPhaseManager().setUpdateTask(updateTask);
        }

        game.getStageChangingToBoss().compareAndSet(true, false);
    }
}
