package com.jftse.emulator.server.core.handler.matchplay.start;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.life.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplaySetPlayerPosition;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.server.core.protocol.Packet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StartBattleModeHandler extends AbstractPacketHandler {
    private final Random random;

    private final RunnableEventHandler runnableEventHandler;

    public StartBattleModeHandler() {
        random = new Random();

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getMatchplayGame();

        List<FTClient> clients = new ArrayList<>(gameSession.getClients());
        List<PlayerPositionInfo> positionInfo = new ArrayList<>();

        clients.forEach(c -> {
            RoomPlayer rp = c.getRoomPlayer();
            if (rp == null || rp.getPosition() > 3) {
                return;
            }

            Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
            PlayerPositionInfo playerPositionInfo = new PlayerPositionInfo();
            playerPositionInfo.setPlayerPosition(rp.getPosition());
            playerPositionInfo.setPlayerStartLocation(playerLocation);
            positionInfo.add(playerPositionInfo);
        });

        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplaySetPlayerPosition setPlayerPositionPacket = new S2CMatchplaySetPlayerPosition(positionInfo);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setPlayerPositionPacket, ftClient.getConnection());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());

        long crystalSpawnInterval = TimeUnit.SECONDS.toMillis(8);
        long crystalDeSpawnInterval = TimeUnit.SECONDS.toMillis(10);
        game.setCrystalSpawnInterval(crystalSpawnInterval);
        game.setCrystalDeSpawnInterval(crystalDeSpawnInterval);

        int activePlayers = game.getPlayerBattleStates().size();
        int amountOfCrystalsToSpawnPerSide = activePlayers > 2 ? 2 : 1;
        for (int i = 0; i < amountOfCrystalsToSpawnPerSide; i++) {
            RunnableEvent placeCrystalEventRedTeam = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(ftClient.getConnection(), GameFieldSide.RedTeam), crystalDeSpawnInterval);
            RunnableEvent placeCrystalEventBlueTeam = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(ftClient.getConnection(), GameFieldSide.BlueTeam), crystalDeSpawnInterval);

            gameSession.getRunnableEvents().add(placeCrystalEventRedTeam);
            gameSession.getRunnableEvents().add(placeCrystalEventBlueTeam);
        }
    }
}
