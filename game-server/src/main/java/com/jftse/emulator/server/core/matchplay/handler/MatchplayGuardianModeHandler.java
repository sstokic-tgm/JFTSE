package com.jftse.emulator.server.core.matchplay.handler;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.client.PlayerStatisticView;
import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
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
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.guardian.PhaseManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardianStats;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSetGuardians;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.rabbit.messages.MatchFinishedMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.core.task.AutoItemRewardPickerTask;
import com.jftse.emulator.server.core.task.DefeatTimerTask;
import com.jftse.emulator.server.core.task.GuardianAttackTask;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.model.battle.Guardian2Maps;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.scenario.MScenarios;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import com.jftse.server.core.shared.packets.item.SMSGItemSettings;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeMap;
import com.jftse.server.core.shared.packets.matchplay.CMSGPoint;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import javax.persistence.TypedQuery;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class MatchplayGuardianModeHandler implements MatchplayHandleable {
    private final MatchplayGuardianGame game;

    private final Random random;

    private final GameLogService gameLogService;
    private final EventHandler eventHandler;
    private final LevelService levelService;
    private final GuardianService guardianService;
    private final BossGuardianService bossGuardianService;
    private final ScenarioService scenarioService;
    private final PlayerStatisticService playerStatisticService;
    private final MapService mapService;

    private final JdbcUtil jdbcUtil;

    public MatchplayGuardianModeHandler(MatchplayGuardianGame game) {
        this.game = game;
        this.random = new Random();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
        this.eventHandler = GameManager.getInstance().getEventHandler();
        this.levelService = ServiceManager.getInstance().getLevelService();
        this.guardianService = ServiceManager.getInstance().getGuardianService();
        this.bossGuardianService = ServiceManager.getInstance().getBossGuardianService();
        this.scenarioService = ServiceManager.getInstance().getScenarioService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.mapService = ServiceManager.getInstance().getMapService();

        this.jdbcUtil = ServiceManager.getInstance().getJdbcUtil();
    }

    @Override
    public void onStart(FTClient ftClient) {
        final GameSession gameSession = ftClient.getActiveGameSession();

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, servingPositionXOffset, servingPositionYOffset);
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

        SMSGItemSettings requestItemSettings = SMSGItemSettings.builder().build();
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(requestItemSettings, ftClient.getConnection());

        ThreadManager.getInstance().newTask(new GuardianAttackTask(ftClient.getConnection()));
        ThreadManager.getInstance().newTask(new DefeatTimerTask(ftClient.getConnection(), gameSession));
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
        gameSession.getFireables().forEach(f -> f.setCancelled(true));
        gameSession.getFireables().clear();

        if (game.isAdvancedBossGuardianMode()) {
            PhaseManager phaseManager = game.getPhaseManager();
            phaseManager.end();
        }

        final boolean allPlayersDead = game.getPlayerBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allGuardiansDead = game.getGuardianBattleStates().stream().allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean wonGame = allGuardiansDead && !allPlayersDead;
        final int secondsPlayed = (int) Math.ceil((double) game.getTimeNeeded() / 1000);

        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();

        StringBuilder gameLogContent = new StringBuilder();

        if (game.getBossBattleActive().get() && allGuardiansDead && !allPlayersDead) {
            final long timeNeededSeconds = TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded());
            final List<Integer> stages = List.of(9, 10, 13, 14);

            boolean underSixty = (timeNeededSeconds < 60) && !stages.contains(game.getMap().getMap());
            boolean underNinety = (timeNeededSeconds < 90) && stages.contains(game.getMap().getMap());

            if (underSixty || underNinety) {
                gameLogContent.append("Boss Guardian finished before ");
                if (underNinety)
                    gameLogContent.append("90s. ");
                if (underSixty)
                    gameLogContent.append("60s. ");
                gameLogContent.append(game.getMap().getName()).append(" ");

                for (FTClient client : clients) {
                    RoomPlayer rp = client.getRoomPlayer();
                    if (rp == null)
                        continue;

                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
                    client.getConnection().sendTCP(msgPacket);
                    client.getConnection().close();

                    gameLogContent.append(rp.getName()).append(" acc: ").append(rp.getAccountId()).append("; ");
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

        MatchplayReward matchplayReward = game.getMatchRewards();
        game.addBonusesToRewards(activeRoom.getRoomPlayerList(), matchplayReward.getPlayerRewards());

        GameSessionManager.getInstance().addMatchplayReward(activeRoom.getRoomId(), matchplayReward);

        gameLogContent = new StringBuilder();

        gameLogContent.append(game.getMap().getName()).append(" ");
        gameLogContent.append(game.getBossBattleActive().get() ? "Boss " : "Guardian ").append("battle finished. ");
        gameLogContent.append(wonGame ? "Players " : "Guardians ").append("won. ");

        List<MatchFinishedMessage.PlayerDto> playerDtoList = new ArrayList<>();

        for (final FTClient client : clients) {
            if (!client.hasPlayer())
                continue;

            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            final boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                gameLogContent.append(rp.getName()).append(" acc: ").append(rp.getAccountId()).append("; ");

                PlayerReward playerReward = matchplayReward.getPlayerReward(rp.getPosition());
                if (playerReward == null) {
                    playerReward = new PlayerReward(rp.getPosition());
                }

                FTPlayer player = client.getPlayer();

                playerDtoList.add(new MatchFinishedMessage.PlayerDto(player.getName(), "red"));

                List<BaseItem> ringItemList = new ArrayList<>();
                if (!rp.isRingOfWisemanEquipped()) {
                    if (rp.isRingOfExpEquipped()) {
                        RingOfExp ringOfExp = (RingOfExp) ItemFactory.getItem(rp.getPpIdRingExp(), player.getPocketId());
                        if (ringOfExp != null) {
                            ringItemList.add(ringOfExp);
                        }
                    }
                    if (rp.isRingOfGoldEquipped()) {
                        RingOfGold ringOfGold = (RingOfGold) ItemFactory.getItem(rp.getPpIdRingGold(), player.getPocketId());
                        if (ringOfGold != null) {
                            ringItemList.add(ringOfGold);
                        }
                    }
                } else {
                    RingOfWiseman ringOfWiseman = (RingOfWiseman) ItemFactory.getItem(rp.getPpIdRingWiseman(), player.getPocketId());
                    if (ringOfWiseman != null) {
                        ringItemList.add(ringOfWiseman);
                    }
                }

                for (BaseItem ring : ringItemList) {
                    if (ring.processPlayer(player) && ring.processPocket(player.getPocketId())) {
                        ring.getPacketsToSend().forEach((playerId, packets) -> {
                            final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
                            if (connectionByPlayerId != null)
                                connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
                        });
                    }
                }

                final int oldLevel = player.getLevel();
                final int level = levelService.getLevel(playerReward.getExp(), player.getExpPoints(), (byte) oldLevel);
                if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
                    player.syncExpPoints(player.getExpPoints() + playerReward.getExp());
                player.syncGold(player.getGold() + playerReward.getGold());
                player.syncCouplePoints(player.getCouplePoints() + playerReward.getCouplePoints());
                levelService.setNewLevelStatusPoints((byte) level, player.getPlayer());
                player.syncLevel(level);

                PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatisticId());
                if (wonGame) {
                    playerStatistic.setGuardianRecordWin(playerStatistic.getGuardianRecordWin() + 1);
                } else {
                    playerStatistic.setGuardianRecordLoss(playerStatistic.getGuardianRecordLoss() + 1);
                }

                final ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates = game.getGuardianBattleStates();
                List<Integer> guardianRewardRankingPointList = guardianBattleStates.stream()
                        .filter(g -> g.getLooted().get())
                        .map(GuardianBattleState::getRewardRankingPoint)
                        .toList();

                if (wonGame) {
                    final int guardianRewardRankingPointSum = guardianRewardRankingPointList.stream()
                            .mapToInt(v -> {
                                if (game.getIsHardMode().get() && !game.getIsRandomGuardiansMode().get()) {
                                    return (int) (v + (v * ConfigService.getInstance().getValue("matchplay.guardian.hard.won.ranking-point.multiplier", 1.0)));
                                }
                                return v;
                            })
                            .sum();

                    playerReward.setRankingPoints(guardianRewardRankingPointSum);
                } else {
                    if (game.getMap().getIsBossStage() && secondsPlayed < 90 && !game.getBossBattleActive().get()) {
                        guardianRewardRankingPointList = guardianBattleStates.stream()
                                .map(GuardianBattleState::getRewardRankingPoint)
                                .toList();

                        final int guardianRewardRankingPointSum = guardianRewardRankingPointList.stream()
                                .mapToInt(v -> {
                                    if (game.getIsHardMode().get() && !game.getIsRandomGuardiansMode().get()) {
                                        return (int) (v + (v * ConfigService.getInstance().getValue("matchplay.guardian.hard.lost.ranking-point.multiplier", 1.0)));
                                    }
                                    return v;
                                })
                                .sum();

                        playerReward.setRankingPoints(-guardianRewardRankingPointSum);
                    }
                }
                playerStatistic.setGuardianRP(playerStatistic.getGuardianRP() + playerReward.getRankingPoints());

                playerStatistic = playerStatisticService.save(playerStatistic);
                player.setPlayerStatistic(PlayerStatisticView.fromEntity(playerStatistic));

                rp.setReady(false);
                int playerLevel = player.getLevel();
                byte resultTitle = (byte) (wonGame ? 1 : 0);
                if (playerLevel != oldLevel) {
                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player);
                    eventHandler.offer(eventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0));
                }

                S2CMatchplayItemRewardsPacket itemRewardsPacket = new S2CMatchplayItemRewardsPacket(matchplayReward);
                client.getConnection().sendTCP(itemRewardsPacket);

                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, secondsPlayed, playerReward, (byte) playerLevel, rp);
                eventHandler.offer(eventHandler.createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0));
            } else {
                gameLogContent.append("spec: ").append(rp.getName()).append(" acc: ").append(rp.getAccountId()).append("; ");

                if (rp.getPosition() != MiscConstants.InvisibleGmSlot) {
                    playerDtoList.add(new MatchFinishedMessage.PlayerDto(rp.getName(), "spectator"));
                }
            }

            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(matchplayReward.getPlayerRewards());
            eventHandler.offer(eventHandler.createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0));

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            eventHandler.offer(eventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)));
            client.setActiveGameSession(null);
        }

        GameEventBus.call(GameEventType.MP_MATCH_END, game, activeRoom, clients);

        eventHandler.offer(eventHandler.createRunnableEvent(new AutoItemRewardPickerTask(new ConcurrentLinkedDeque<>(clients), activeRoom.getRoomId()), TimeUnit.SECONDS.toMillis(9)));

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

            MatchFinishedMessage message = MatchFinishedMessage.builder()
                    .gameSessionId(gameSessionId)
                    .time(game.getTimeNeeded())
                    .mode("GUARDIAN")
                    .winner(wonGame ? "red" : "blue")
                    .map(game.getMap().getName())
                    .players(playerDtoList)
                    .isBoss(game.getBossBattleActive().get())
                    .isRandom(game.getIsRandomGuardiansMode().get())
                    .isHard(game.getIsHardMode().get())
                    .build();
            RProducerService.getInstance().send(message, "game.stats.match", "MatchplaySystem");
        }
    }

    @Override
    public void onPrepare(FTClient ftClient) {
        Room room = ftClient.getActiveRoom();

        game.getIsHardMode().set(room.isHardMode());
        game.getIsRandomGuardiansMode().set(room.isRandomGuardians());

        ConcurrentLinkedDeque<RoomPlayer> roomPlayers = room.getRoomPlayerList();

        float averagePlayerLevel = this.getAveragePlayerLevel(new ArrayList<>(roomPlayers));
        this.handleMonsLavaMap(ftClient.getConnection(), room, averagePlayerLevel);


        Optional<SMaps> map = mapService.findByMap((int) room.getMap());
        if (map.isEmpty()) {
            log.error("No map found for mapId: " + room.getMap());
            return;
        }
        game.setMap(map.get());

        MScenarios scenario = scenarioService.getDefaultScenarioByMapAndGameMode(game.getMap().getId(), MScenarios.GameMode.GUARDIAN);
        if (scenario == null) {
            log.error("No default scenario found for game mode: " + MScenarios.GameMode.GUARDIAN);
            return;
        }
        game.setScenario(scenario);

        jdbcUtil.execute(em -> {
            TypedQuery<Guardian2Maps> q = em.createQuery("SELECT g FROM Guardian2Maps g WHERE g.map.id = :mapId AND g.scenario.id = :scenarioId AND g.status.id = 1", Guardian2Maps.class);
            q.setParameter("mapId", game.getMap().getId());
            q.setParameter("scenarioId", game.getScenario().getId());
            List<Guardian2Maps> guardian2Maps = q.getResultList();
            game.getGuardiansInStage().addAll(guardian2Maps);
        });

        if (game.getMap().getIsBossStage()) {

            List<MScenarios> scenarios = scenarioService.getByMapAndIsDefault(game.getMap().getId(), true);
            if (scenarios.isEmpty()) {
                log.error("No default scenarios found for map: " + game.getMap().getName());
                return;
            }

            MScenarios bossScenario = scenarios.stream()
                    .filter(s -> s.getGameMode() == MScenarios.GameMode.BOSS_BATTLE || s.getGameMode() == MScenarios.GameMode.BOSS_BATTLE_V2)
                    .findFirst()
                    .orElse(null);

            if (bossScenario == null) {
                log.error("No boss scenario found for map: " + game.getMap().getName());
                return;
            }

            jdbcUtil.execute(em -> {
                TypedQuery<Guardian2Maps> q = em.createQuery("SELECT g FROM Guardian2Maps g WHERE g.map.id = :mapId AND g.scenario.id = :scenarioId AND g.status.id = 1", Guardian2Maps.class);
                q.setParameter("mapId", game.getMap().getId());
                q.setParameter("scenarioId", bossScenario.getId());
                List<Guardian2Maps> guardian2Maps = q.getResultList();
                game.getGuardiansInBossStage().addAll(guardian2Maps);
            });
        }

        int guardianLevelLimit = this.getGuardianLevelLimit(averagePlayerLevel);
        game.getGuardianLevelLimit().set(guardianLevelLimit);

        roomPlayers.forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });

        int activePlayingPlayersCount = (int) roomPlayers.stream().filter(x -> x.getPosition() < 4).count();
        byte guardianStartPosition = 10;
        List<GuardianBase> guardians = game.determineGuardians(game.getGuardiansInStage(), game.getGuardianLevelLimit().get());

        if (room.isHardMode()) {
            game.fillRemainingGuardianSlots(false, game, game.getGuardiansInStage(), guardians);
        }

        for (int i = 0; i < guardians.size(); i++) {
            GuardianBase guardianBase = guardians.get(i);
            if (guardianBase == null) continue;

            if (game.getIsRandomGuardiansMode().get()) {
                guardianBase.setId((long) (Math.random() * 72 + 1));
            }

            short guardianPosition = (short) (i + guardianStartPosition);
            if (guardianBase instanceof Guardian) {
                guardianBase = guardianService.findGuardianById(guardianBase.getId());
            } else {
                guardianBase = bossGuardianService.findBossGuardianById(guardianBase.getId());
            }
            if (game.getIsRandomGuardiansMode().get()) {
                guardians.set(i, guardianBase);
            }

            GuardianBattleState guardianBattleState = game.createGuardianBattleState(game.getIsHardMode().get(), guardianBase, guardianPosition, activePlayingPlayersCount);
            game.getGuardianBattleStates().add(guardianBattleState);
        }

        S2CRoomSetGuardians roomSetGuardians = new S2CRoomSetGuardians(guardians.get(0), guardians.get(1), guardians.get(2));
        S2CRoomSetGuardianStats roomSetGuardianStats = new S2CRoomSetGuardianStats(game.getGuardianBattleStates(), guardians);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardians, ftClient.getConnection());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomSetGuardianStats, ftClient.getConnection());
    }

    @Override
    public void onPoint(FTClient ftClient, CMSGPoint pointPacket) {
        boolean lastGuardianServeWasOnGuardianSide = game.getLastGuardianServeSide().get() == GameFieldSide.Guardian;

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket;
        if (!lastGuardianServeWasOnGuardianSide) {
            game.getLastGuardianServeSide().set(GameFieldSide.Guardian);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Guardian, servingPositionXOffset, servingPositionYOffset);
        } else {
            game.getLastGuardianServeSide().set(GameFieldSide.Players);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe(GameFieldSide.Players, servingPositionXOffset, servingPositionYOffset);
        }
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());
    }

    private float getAveragePlayerLevel(final List<RoomPlayer> roomPlayers) {
        List<RoomPlayer> activePlayingPlayers = roomPlayers.stream().filter(x -> x.getPosition() < 4).toList();
        List<Integer> playerLevels = activePlayingPlayers.stream().map(RoomPlayer::getLevel).toList();
        int levelSum = playerLevels.stream().reduce(0, Integer::sum);
        return (float) (levelSum / activePlayingPlayers.size());
    }

    private void handleMonsLavaMap(FTConnection connection, Room room, float averagePlayerLevel) {
        boolean isMonsLava = room.getMap() == 7 || room.getMap() == 8;
        int monsLavaBProbability = random.nextInt(101);
        if (isMonsLava && averagePlayerLevel >= 40 && monsLavaBProbability <= 26) {
            room.setMap((byte) 8); // MonsLavaB
            SMSGRoomChangeMap roomChangeMapPacket = SMSGRoomChangeMap.builder().map(room.getMap()).build();
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomChangeMapPacket, connection);
        } else if (room.getMap() == 8) {
            room.setMap((byte) 7); // MonsLava
            SMSGRoomChangeMap roomChangeMapPacket = SMSGRoomChangeMap.builder().map(room.getMap()).build();
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(roomChangeMapPacket, connection);
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
