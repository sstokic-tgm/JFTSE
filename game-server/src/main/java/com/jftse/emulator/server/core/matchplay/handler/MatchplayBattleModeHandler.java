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
import com.jftse.emulator.server.core.life.match.PlayerStats;
import com.jftse.emulator.server.core.life.match.RallyResult;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.rabbit.MatchRallyStatsConsumer;
import com.jftse.emulator.server.core.rabbit.messages.MatchFinishedMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.core.task.AutoItemRewardPickerTask;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.matchplay.CMSGPoint;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Log4j2
public class MatchplayBattleModeHandler implements MatchplayHandleable {
    private final MatchplayBattleGame game;


    private final GameLogService gameLogService;
    private final EventHandler eventHandler;
    private final LevelService levelService;
    private final PlayerStatisticService playerStatisticService;

    private final MapService mapService;

    private final MatchRallyStatsConsumer matchRallyStatsConsumer;

    public MatchplayBattleModeHandler(MatchplayBattleGame game) {
        this.game = game;
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
        this.eventHandler = GameManager.getInstance().getEventHandler();
        this.levelService = ServiceManager.getInstance().getLevelService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.mapService = ServiceManager.getInstance().getMapService();
        this.matchRallyStatsConsumer = GameManager.getInstance().getMatchRallyStatsConsumer();
    }

    @Override
    public void onStart(FTClient ftClient) {
        final GameSession gameSession = ftClient.getActiveGameSession();
        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();

        final List<PlayerPositionInfo> positionInfo = new ArrayList<>();
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

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplaySetPlayerPosition setPlayerPositionPacket = new S2CMatchplaySetPlayerPosition(positionInfo);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, servingPositionXOffset, servingPositionYOffset);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setPlayerPositionPacket, ftClient.getConnection());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());

        long crystalSpawnInterval = TimeUnit.SECONDS.toMillis(8);
        long crystalDeSpawnInterval = TimeUnit.SECONDS.toMillis(10);
        game.getCrystalSpawnInterval().set(crystalSpawnInterval);
        game.getCrystalDeSpawnInterval().set(crystalDeSpawnInterval);

        int activePlayers = game.getPlayerBattleStates().size();
        int amountOfCrystalsToSpawnPerSide = activePlayers > 2 ? 2 : 1;
        for (int i = 0; i < amountOfCrystalsToSpawnPerSide; i++) {
            RunnableEvent placeCrystalEventRedTeam = eventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(ftClient.getConnection(), GameFieldSide.RedTeam), crystalDeSpawnInterval);
            RunnableEvent placeCrystalEventBlueTeam = eventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(ftClient.getConnection(), GameFieldSide.BlueTeam), crystalDeSpawnInterval);

            gameSession.getFireables().push(placeCrystalEventRedTeam);
            gameSession.getFireables().push(placeCrystalEventBlueTeam);
            eventHandler.offer(placeCrystalEventRedTeam);
            eventHandler.offer(placeCrystalEventBlueTeam);
        }
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

        gameSession.getFireables().forEach(f -> f.setCancelled(true));
        gameSession.getFireables().clear();

        StringBuilder gameLogContent = new StringBuilder();
        gameLogContent.append("Battle game finished. ");

        final boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);

        gameLogContent.append(allPlayersTeamRedDead ? "Blue " : "Red ").append("team won. ");

        MatchplayReward matchplayReward = game.getMatchRewards();
        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();
        List<FTPlayer> playerList = clients.stream()
                .filter(FTClient::hasPlayer)
                .map(FTClient::getPlayer)
                .collect(Collectors.toList());

        game.addBonusesToRewards(activeRoom.getRoomPlayerList(), matchplayReward.getPlayerRewards());

        GameSessionManager.getInstance().addMatchplayReward(activeRoom.getRoomId(), matchplayReward);

        List<MatchFinishedMessage.PlayerDto> playerDtoList = new ArrayList<>();

        for (final FTClient client : clients) {
            if (!client.hasPlayer())
                continue;

            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());

            if (isActivePlayer) {
                gameLogContent.append(isCurrentPlayerInRedTeam ? "red " : "blue ").append(rp.getName()).append(" acc: ").append(rp.getAccountId()).append("; ");

                final boolean wonGame = isCurrentPlayerInRedTeam && allPlayersTeamBlueDead || !isCurrentPlayerInRedTeam && allPlayersTeamRedDead;

                PlayerReward playerReward = matchplayReward.getPlayerReward(rp.getPosition());
                if (playerReward == null) {
                    playerReward = new PlayerReward(rp.getPosition());
                }

                FTPlayer player = client.getPlayer();

                playerDtoList.add(new MatchFinishedMessage.PlayerDto(player.getName(), isCurrentPlayerInRedTeam ? "red" : "blue"));

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

                PlayerStatisticView playerStatistic = player.getPlayerStatistic();

                PlayerStats playerStats = matchRallyStatsConsumer.getPlayerStats(gameSessionId, Math.toIntExact(player.getId()));
                HashMap<Long, Integer> playerRatings = RankingUtils.calculateNewRating(playerList, player, wonGame, (byte) GameMode.BATTLE);
                int playerRankingPoints = playerRatings.get(player.getId()) - playerStatistic.battleRP();
                int playerNewRating = playerRatings.get(player.getId());

                playerReward.setRankingPoints(playerRankingPoints);

                PlayerStatistic dbPlayerStatistic = playerStatisticService.updatePlayerStats(player.getPlayerStatisticId(), GameMode.BATTLE, wonGame,
                        playerNewRating, 0, 0, playerStats.getStroke(), playerStats.getSlice(), playerStats.getLob(),
                        playerStats.getSmash(), playerStats.getVolley(), playerStats.getTopSpin(), playerStats.getRising(),
                        playerStats.getServe(), playerStats.getGuardBreakShot(), playerStats.getChargeShot(), playerStats.getSkillShot());

                player.setPlayerStatistic(PlayerStatisticView.fromEntity(dbPlayerStatistic));

                rp.setReady(false);
                int playerLevel = player.getLevel();
                byte resultTitle = (byte) (wonGame ? 1 : 0);
                if (playerLevel != oldLevel) {
                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player);
                    eventHandler.offer(eventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0));
                }

                S2CMatchplayItemRewardsPacket itemRewardsPacket = new S2CMatchplayItemRewardsPacket(matchplayReward);
                client.getConnection().sendTCP(itemRewardsPacket);

                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, (byte) playerLevel, rp);
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

        matchRallyStatsConsumer.clearSession(gameSessionId);

        GameEventBus.call(GameEventType.MP_MATCH_END, game, activeRoom, clients);

        eventHandler.offer(eventHandler.createRunnableEvent(new AutoItemRewardPickerTask(new ConcurrentLinkedDeque<>(clients), activeRoom.getRoomId()), TimeUnit.SECONDS.toMillis(9)));

        gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.BATTLE_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);

            MatchFinishedMessage message = MatchFinishedMessage.builder()
                    .gameSessionId(gameSessionId)
                    .time(game.getTimeNeeded())
                    .mode("BATTLE")
                    .winner(allPlayersTeamRedDead ? "blue" : "red")
                    .map(game.getMap().getName())
                    .players(playerDtoList)
                    .isBoss(false)
                    .isRandom(false)
                    .isHard(false)
                    .build();
            RProducerService.getInstance().send(message, "game.stats.match", "MatchplaySystem");
        }
    }

    @Override
    public void onPrepare(FTClient ftClient) {
        Room room = ftClient.getActiveRoom();

        room.getRoomPlayerList().forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });

        Optional<SMaps> map = mapService.findByMap((int) room.getMap());
        if (map.isEmpty()) {
            log.error("No map found for mapId: " + room.getMap());
            return;
        }
        game.setMap(map.get());
    }

    @Override
    public void onPoint(FTClient ftClient, CMSGPoint pointPacket) {
        boolean lastGuardianServeWasOnBlueTeamsSide = game.getLastGuardianServeSide().get() == GameFieldSide.BlueTeam;

        // winner doesn't matter as service or return ace doesn't exist in battle mode, so we can just pass false
        RallyResult rallyResult = matchRallyStatsConsumer.onPoint(ftClient.getGameSessionId(), false);

        byte servingPositionXOffset = (byte) ServingPositionGenerator.randomServingPositionXOffset();
        byte servingPositionYOffset = (byte) ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket;
        if (!lastGuardianServeWasOnBlueTeamsSide) {
            game.getLastGuardianServeSide().set(GameFieldSide.BlueTeam);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.BlueTeam, servingPositionXOffset, servingPositionYOffset);
        } else {
            game.getLastGuardianServeSide().set(GameFieldSide.RedTeam);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, servingPositionXOffset, servingPositionYOffset);
        }
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());
    }
}
