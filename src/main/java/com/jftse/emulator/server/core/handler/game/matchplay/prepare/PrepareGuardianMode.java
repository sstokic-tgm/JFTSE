package com.jftse.emulator.server.core.handler.game.matchplay.prepare;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomSetGuardianStats;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomSetGuardians;
import com.jftse.emulator.server.core.service.GuardianService;
import com.jftse.emulator.server.core.service.GuardianStageService;
import com.jftse.emulator.server.core.service.WillDamageService;
import com.jftse.emulator.server.database.model.battle.Guardian;
import com.jftse.emulator.server.database.model.battle.GuardianStage;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PrepareGuardianMode extends AbstractHandler {
    private final WillDamageService willDamageService;
    private final GuardianStageService guardianStageService;
    private final GuardianService guardianService;

    public PrepareGuardianMode() {
        this.willDamageService = ServiceManager.getInstance().getWillDamageService();
        this.guardianStageService = ServiceManager.getInstance().getGuardianStageService();
        this.guardianService = ServiceManager.getInstance().getGuardianService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveRoom() == null || connection.getClient().getActiveGameSession() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGuardianGame game = (MatchplayGuardianGame) gameSession.getActiveMatchplayGame();

        game.setHardMode(room.isHardMode());
        game.setRandomGuardiansMode(room.isRandomGuardians());
        game.setWillDamages(willDamageService.getWillDamages());

        ArrayList<RoomPlayer> roomPlayers = new ArrayList<>(room.getRoomPlayerList());

        float averagePlayerLevel = this.getAveragePlayerLevel(new ArrayList<>(roomPlayers));
        this.handleMonsLavaMap(connection, room, averagePlayerLevel);

        GuardianStage guardianStage = guardianStageService.getGuardianStages().stream()
                .filter(x -> x.getMapId() == room.getMap() && !x.getIsBossStage())
                .findFirst()
                .orElse(null);
        game.setGuardianStage(guardianStage);
        game.setCurrentStage(guardianStage);

        GuardianStage bossGuardianStage = guardianStageService.getGuardianStages().stream()
                .filter(x -> x.getMapId() == room.getMap() && x.getIsBossStage())
                .findFirst()
                .orElse(null);
        game.setBossGuardianStage(bossGuardianStage);

        int guardianLevelLimit = this.getGuardianLevelLimit(averagePlayerLevel);
        game.setGuardianLevelLimit(guardianLevelLimit);

        roomPlayers.forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });

        int activePlayingPlayersCount = (int) roomPlayers.stream().filter(x -> x.getPosition() < 4).count();
        byte guardianStartPosition = 10;
        List<Byte> guardians = game.determineGuardians(game.getGuardianStage(), game.getGuardianLevelLimit());

        if (room.isHardMode()) {
            game.fillRemainingGuardianSlots(false, game, guardianStage, guardians);
        }

        for (int i = 0; i < (long) guardians.size(); i++) {
            int guardianId = guardians.get(i);
            if (guardianId == 0) continue;

            if (game.isRandomGuardiansMode()) {
                guardianId = (int) (Math.random() * 72 + 1);
                guardians.set(i, (byte) guardianId);
            }

            short guardianPosition = (short) (i + guardianStartPosition);
            Guardian guardian = guardianService.findGuardianById((long) guardianId);
            GuardianBattleState guardianBattleState = game.createGuardianBattleState(game.isHardMode(), guardian, guardianPosition, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(guardianBattleState);
        }

        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians(guardians.get(0), guardians.get(1), guardians.get(2));
        S2CRoomSetGuardianStats roomSetGuardianStats = new S2CRoomSetGuardianStats(game.getGuardianBattleStates(), guardians);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardians, connection);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardianStats, connection);
    }

    private float getAveragePlayerLevel(List<RoomPlayer> roomPlayers) {
        List<RoomPlayer> activePlayingPlayers = roomPlayers.stream().filter(x -> x.getPosition() < 4).collect(Collectors.toList());
        List<Integer> playerLevels = activePlayingPlayers.stream().map(x -> (int) x.getPlayer().getLevel()).collect(Collectors.toList());
        int levelSum = playerLevels.stream().reduce(0, Integer::sum);
        return (float) (levelSum / activePlayingPlayers.size());
    }

    private void handleMonsLavaMap(Connection connection, Room room, float averagePlayerLevel) {
        boolean isMonsLava = room.getMap() == 7 || room.getMap() == 8;
        final Random random = new Random();
        int monsLavaBProbability = random.nextInt(101);
        if (isMonsLava && averagePlayerLevel >= 40 && monsLavaBProbability <= 26) {
            room.setMap((byte) 8); // MonsLavaB
            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(room.getMap());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomMapChangeAnswerPacket, connection);
        } else if (room.getMap() == 8) {
            room.setMap((byte) 7); // MonsLava
            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(room.getMap());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomMapChangeAnswerPacket, connection);
        }
    }

    private int getGuardianLevelLimit(float averagePlayerLevel) {
        int minGuardianLevelLimit = 10;
        int roundLevel = 5 * (Math.round(averagePlayerLevel / 5));
        if (roundLevel < averagePlayerLevel) {
            if (averagePlayerLevel < minGuardianLevelLimit) return minGuardianLevelLimit;
            return (int) averagePlayerLevel;
        }

        return Math.max(roundLevel, minGuardianLevelLimit);
    }
}
