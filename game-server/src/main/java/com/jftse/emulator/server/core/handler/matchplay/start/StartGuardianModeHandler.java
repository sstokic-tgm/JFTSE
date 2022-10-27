package com.jftse.emulator.server.core.handler.matchplay.start;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.task.DefeatTimerTask;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.thread.ThreadManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StartGuardianModeHandler extends AbstractPacketHandler {
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
        FTClient ftClient = connection.getClient();
        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getMatchplayGame();

        int servingPositionXOffset = random.nextInt(7);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());

        game.resetStageStartTime();

        int activePlayers = game.getPlayerBattleStates().size();
        switch (activePlayers) {
            case 1, 2 -> {
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(8));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
            }
            case 3, 4 -> {
                game.setCrystalSpawnInterval(TimeUnit.SECONDS.toMillis(5));
                game.setCrystalDeSpawnInterval(TimeUnit.SECONDS.toMillis(7));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
            }
        }

        ThreadManager.getInstance().newTask(new GuardianAttackTask(ftClient.getConnection()));
        ThreadManager.getInstance().newTask(new DefeatTimerTask(ftClient.getConnection(), gameSession, game.getGuardianStage()));
    }
}
