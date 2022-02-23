package com.jftse.emulator.server.core.handler.game.matchplay.start;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.core.task.DefeatTimerTask;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StartGuardianModeHandler extends AbstractHandler {
    private final Random random;

    public StartGuardianModeHandler() {
        random = new Random();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        int servingPositionXOffset = random.nextInt(7);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, connection);

        game.resetStageStartTime();

        int activePlayers = game.getPlayerBattleStates().size();
        switch (activePlayers) {
            case 1, 2 -> {
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(8));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(connection));
            }
            case 3, 4 -> {
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(7));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(connection));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(connection));
            }
        }

        ThreadManager.getInstance().newTask(new GuardianAttackTask(connection));
        ThreadManager.getInstance().newTask(new DefeatTimerTask(connection, gameSession, game.getGuardianStage()));
    }
}
