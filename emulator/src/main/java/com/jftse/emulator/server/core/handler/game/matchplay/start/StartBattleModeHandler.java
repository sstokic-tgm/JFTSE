package com.jftse.emulator.server.core.handler.game.matchplay.start;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplaySetPlayerPosition;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerGuardianServe;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StartBattleModeHandler extends AbstractHandler {
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
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBattleGame game = (MatchplayBattleGame) gameSession.getActiveMatchplayGame();

        ArrayList<Client> clients = new ArrayList<>(gameSession.getClients());
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
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setPlayerPositionPacket, connection);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, connection);

        long crystalSpawnInterval = TimeUnit.SECONDS.toMillis(8);
        long crystalDeSpawnInterval = TimeUnit.SECONDS.toMillis(10);
        game.setCrystalSpawnInterval(crystalSpawnInterval);
        game.setCrystalDeSpawnInterval(crystalDeSpawnInterval);

        int activePlayers = game.getPlayerBattleStates().size();
        int amountOfCrystalsToSpawnPerSide = activePlayers > 2 ? 2 : 1;
        for (int i = 0; i < amountOfCrystalsToSpawnPerSide; i++) {
            RunnableEvent placeCrystalEventRedTeam = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection, GameFieldSide.RedTeam), crystalDeSpawnInterval);
            RunnableEvent placeCrystalEventBlueTeam = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection, GameFieldSide.BlueTeam), crystalDeSpawnInterval);

            gameSession.getRunnableEvents().add(placeCrystalEventRedTeam);
            gameSession.getRunnableEvents().add(placeCrystalEventBlueTeam);
        }
    }
}
