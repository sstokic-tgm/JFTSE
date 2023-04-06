package com.jftse.emulator.server.core.matchplay.handler;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.item.special.RingOfExp;
import com.jftse.emulator.server.core.life.item.special.RingOfGold;
import com.jftse.emulator.server.core.life.item.special.RingOfWiseman;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardianStats;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardians;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.task.DefeatTimerTask;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.model.battle.GuardianStage;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import com.jftse.server.core.thread.ThreadManager;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MatchplayGuardianModeHandler implements MatchplayHandleable {
    private final MatchplayGuardianGame game;

    private final Random random;

    private final GameLogService gameLogService;
    private final EventHandler eventHandler;
    private final LevelService levelService;
    private final PlayerPocketService playerPocketService;
    private final ClothEquipmentService clothEquipmentService;
    private final WillDamageService willDamageService;
    private final GuardianService guardianService;
    private final GuardianStageService guardianStageService;

    public MatchplayGuardianModeHandler(MatchplayGuardianGame game) {
        this.game = game;
        this.random = new Random();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
        this.eventHandler = GameManager.getInstance().getEventHandler();
        this.levelService = ServiceManager.getInstance().getLevelService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        this.willDamageService = ServiceManager.getInstance().getWillDamageService();
        this.guardianService = ServiceManager.getInstance().getGuardianService();
        this.guardianStageService = ServiceManager.getInstance().getGuardianStageService();
    }

    @Override
    public void onStart(FTClient ftClient) {
        final GameSession gameSession = ftClient.getActiveGameSession();

        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());

        game.resetStageStartTime();

        int activePlayers = game.getPlayerBattleStates().size();
        switch (activePlayers) {
            case 1, 2 -> {
                game.getCrystalSpawnInterval().set(TimeUnit.SECONDS.toMillis(5));
                game.getCrystalDeSpawnInterval().set(TimeUnit.SECONDS.toMillis(8));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
            }
            case 3, 4 -> {
                game.getCrystalSpawnInterval().set(TimeUnit.SECONDS.toMillis(5));
                game.getCrystalDeSpawnInterval().set(TimeUnit.SECONDS.toMillis(7));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
                ThreadManager.getInstance().newTask(new PlaceCrystalRandomlyTask(ftClient.getConnection()));
            }
        }

        ThreadManager.getInstance().newTask(new GuardianAttackTask(ftClient.getConnection()));
        ThreadManager.getInstance().newTask(new DefeatTimerTask(ftClient.getConnection(), gameSession, game.getGuardianStage()));
    }

    @Override
    public void onEnd(FTClient ftClient) {
        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        final Integer gameSessionId = ftClient.getGameSessionId();

        Room activeRoom = ftClient.getActiveRoom();
        if (activeRoom == null)
            return;

        if (!game.getFinished().compareAndSet(false, true))
            return;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (game.getEndTime() == null)
            game.setEndTime(new AtomicReference<>(cal.getTime()));
        else
            game.getEndTime().set(cal.getTime());

        activeRoom.setStatus(RoomStatus.NotRunning);

        gameSession.clearCountDownRunnable();

        final boolean allPlayersDead = game.getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && !allPlayersDead;

        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();

        StringBuilder gameLogContent = new StringBuilder();

        if (game.getBossBattleActive().get() && allGuardiansDead && !allPlayersDead) {
            final long timeNeededSeconds = TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded());
            final List<Integer> stages = List.of(9, 10, 13, 14);

            boolean underSixty = (timeNeededSeconds < 60) && !stages.contains(game.getCurrentStage().getMapId());
            boolean underNinety = (timeNeededSeconds < 90) && stages.contains(game.getCurrentStage().getMapId());

            if (underSixty || underNinety) {
                gameLogContent.append("Boss Guardian finished before ");
                if (underNinety)
                    gameLogContent.append("90s. ");
                if (underSixty)
                    gameLogContent.append("60s. ");
                gameLogContent.append(game.getCurrentStage().getName()).append(" ");

                for (FTClient client : clients) {
                    RoomPlayer rp = client.getRoomPlayer();
                    if (rp == null)
                        continue;

                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
                    client.getConnection().sendTCP(msgPacket);
                    client.getConnection().close();

                    gameLogContent.append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
                }
                gameLogContent.append("playtime: ").append(timeNeededSeconds).append("s");
                if (game.getIsHardMode().get())
                    gameLogContent.append("; ").append("hard mode");
                if (game.getIsRandomGuardiansMode().get())
                    gameLogContent.append("; ").append("random mode");

                GameLog gameLog = new GameLog();
                gameLog.setGameLogType(GameLogType.BANABLE);
                gameLog.setContent(gameLogContent.toString());
                gameLogService.save(gameLog);

                gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
                if (game.getFinished().get() && gameSession.getClients().isEmpty()) {
                    GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
                }
                return;
            }
        }

        List<PlayerReward> playerRewards = game.getPlayerRewards();
        game.addBonusesToRewards(activeRoom.getRoomPlayerList(), playerRewards);

        gameLogContent = new StringBuilder();

        gameLogContent.append(game.getCurrentStage().getName()).append(" ");
        gameLogContent.append(game.getBossBattleActive().get() ? "Boss " : "Guardian ").append("battle finished. ");
        gameLogContent.append(wonGame ? "Players " : "Guardians ").append("won. ");

        for (FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            final boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                gameLogContent.append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                PlayerReward playerReward = playerRewards.stream()
                        .filter(pr -> pr.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(new PlayerReward(rp.getPosition()));

                Player player = rp.getPlayer();

                List<BaseItem> ringItemList = new ArrayList<>();
                if (!rp.isRingOfWisemanEquipped()) {
                    if (rp.isRingOfExpEquipped()) {
                        final PlayerPocket pp = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(1, EItemCategory.SPECIAL.getName(), player.getPocket());
                        RingOfExp ringOfExp = (RingOfExp) ItemFactory.getItem(pp.getId(), player.getPocket());
                        if (ringOfExp != null) {
                            ringItemList.add(ringOfExp);
                        }
                    }
                    if (rp.isRingOfGoldEquipped()) {
                        final PlayerPocket pp = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(2, EItemCategory.SPECIAL.getName(), player.getPocket());
                        RingOfGold ringOfGold = (RingOfGold) ItemFactory.getItem(pp.getId(), player.getPocket());
                        if (ringOfGold != null) {
                            ringItemList.add(ringOfGold);
                        }
                    }
                } else {
                    final PlayerPocket pp = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(3, EItemCategory.SPECIAL.getName(), player.getPocket());
                    RingOfWiseman ringOfWiseman = (RingOfWiseman) ItemFactory.getItem(pp.getId(), player.getPocket());
                    if (ringOfWiseman != null) {
                        ringItemList.add(ringOfWiseman);
                    }
                }

                for (BaseItem ring : ringItemList) {
                    if (ring.processPlayer(player) && ring.processPocket(player.getPocket())) {
                        ring.getPacketsToSend().forEach((playerId, packets) -> {
                            final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
                            if (connectionByPlayerId != null)
                                connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
                        });
                    }
                }

                final byte oldLevel = player.getLevel();
                final byte level = levelService.getLevel(playerReward.getExp(), player.getExpPoints(), oldLevel);
                if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
                    player.setExpPoints(player.getExpPoints() + playerReward.getExp());
                player.setGold(player.getGold() + playerReward.getGold());
                player = levelService.setNewLevelStatusPoints(level, player);

                player.setCouplePoints(player.getCouplePoints() + playerReward.getCouplePoints());
                client.savePlayer(player);

                if (wonGame) {
                    game.addRewardItemToPocket(client, playerReward);
                }

                rp.setReady(false);
                byte playerLevel = player.getLevel();
                byte resultTitle = (byte) (wonGame ? 1 : 0);
                if (playerLevel != oldLevel) {
                    StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);

                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                    eventHandler.push(eventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0));
                }

                S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);

                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel, rp);
                eventHandler.push(eventHandler.createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0));
            } else {
                gameLogContent.append("spec: ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
            }

            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
            eventHandler.push(eventHandler.createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0));

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            eventHandler.push(eventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)));
            client.setActiveGameSession(null);
        }

        gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

        if (game.getIsHardMode().get())
            gameLogContent.append("; ").append("hard mode");
        if (game.getIsRandomGuardiansMode().get())
            gameLogContent.append("; ").append("random mode");

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.GUARDIAN_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.getFinished().get() && gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
        }
    }

    @Override
    public void onPrepare(FTClient ftClient) {
        Room room = ftClient.getActiveRoom();

        game.getIsHardMode().set(room.isHardMode());
        game.getIsRandomGuardiansMode().set(room.isRandomGuardians());
        game.setWillDamages(willDamageService.getWillDamages());

        ConcurrentLinkedDeque<RoomPlayer> roomPlayers = room.getRoomPlayerList();

        float averagePlayerLevel = this.getAveragePlayerLevel(new ArrayList<>(roomPlayers));
        this.handleMonsLavaMap(ftClient.getConnection(), room, averagePlayerLevel);

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
        game.getGuardianLevelLimit().set(guardianLevelLimit);

        roomPlayers.forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });

        int activePlayingPlayersCount = (int) roomPlayers.stream().filter(x -> x.getPosition() < 4).count();
        byte guardianStartPosition = 10;
        List<Byte> guardians = game.determineGuardians(game.getGuardianStage(), game.getGuardianLevelLimit().get());

        if (room.isHardMode()) {
            game.fillRemainingGuardianSlots(false, game, guardianStage, guardians);
        }

        for (int i = 0; i < (long) guardians.size(); i++) {
            int guardianId = guardians.get(i);
            if (guardianId == 0) continue;

            if (game.getIsRandomGuardiansMode().get()) {
                guardianId = (int) (Math.random() * 72 + 1);
                guardians.set(i, (byte) guardianId);
            }

            short guardianPosition = (short) (i + guardianStartPosition);
            Guardian guardian = guardianService.findGuardianById((long) guardianId);
            GuardianBattleState guardianBattleState = game.createGuardianBattleState(game.getIsHardMode().get(), guardian, guardianPosition, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(guardianBattleState);
        }

        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians(guardians.get(0), guardians.get(1), guardians.get(2));
        S2CRoomSetGuardianStats roomSetGuardianStats = new S2CRoomSetGuardianStats(game.getGuardianBattleStates(), guardians);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardians, ftClient.getConnection());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardianStats, ftClient.getConnection());
    }

    @Override
    public void onPoint(FTClient ftClient, C2SMatchplayPointPacket matchplayPointPacket) {
        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide().get() == GameFieldSide.Guardian;
        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket;
        if (!lastGuardianServeWasOnGuardianSide) {
            game.getLastGuardianServeSide().set(GameFieldSide.Guardian);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, (byte) servingPositionXOffset, (byte) 0);
        } else {
            game.getLastGuardianServeSide().set(GameFieldSide.Players);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, (byte) servingPositionXOffset, (byte) 0);
        }
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());
    }

    private float getAveragePlayerLevel(final List<RoomPlayer> roomPlayers) {
        List<RoomPlayer> activePlayingPlayers = roomPlayers.stream().filter(x -> x.getPosition() < 4).collect(Collectors.toList());
        List<Integer> playerLevels = activePlayingPlayers.stream().map(x -> (int) x.getPlayer().getLevel()).collect(Collectors.toList());
        int levelSum = playerLevels.stream().reduce(0, Integer::sum);
        return (float) (levelSum / activePlayingPlayers.size());
    }

    private void handleMonsLavaMap(FTConnection connection, Room room, float averagePlayerLevel) {
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
