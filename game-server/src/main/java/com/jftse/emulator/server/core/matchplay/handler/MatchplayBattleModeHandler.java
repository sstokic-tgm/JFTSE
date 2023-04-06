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
import com.jftse.emulator.server.core.life.room.PlayerPositionInfo;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MatchplayBattleModeHandler implements MatchplayHandleable {
    private final MatchplayBattleGame game;

    private final Random random;

    private final GameLogService gameLogService;
    private final EventHandler eventHandler;
    private final LevelService levelService;
    private final PlayerPocketService playerPocketService;
    private final PlayerStatisticService playerStatisticService;
    private final ClothEquipmentService clothEquipmentService;
    private final WillDamageService willDamageService;

    public MatchplayBattleModeHandler(MatchplayBattleGame game) {
        this.game = game;
        this.random = new Random();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
        this.eventHandler = GameManager.getInstance().getEventHandler();
        this.levelService = ServiceManager.getInstance().getLevelService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        this.willDamageService = ServiceManager.getInstance().getWillDamageService();
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

        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplaySetPlayerPosition setPlayerPositionPacket = new S2CMatchplaySetPlayerPosition(positionInfo);
        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
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

            eventHandler.push(placeCrystalEventRedTeam);
            eventHandler.push(placeCrystalEventBlueTeam);
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

        StringBuilder gameLogContent = new StringBuilder();
        gameLogContent.append("Battle game finished. ");

        final boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);
        final boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);

        gameLogContent.append(allPlayersTeamRedDead ? "Blue " : "Red ").append("team won. ");

        List<PlayerReward> playerRewards = game.getPlayerRewards();
        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();
        final List<Player> playerList = activeRoom.getRoomPlayerList().stream().map(RoomPlayer::getPlayer).collect(Collectors.toList());

        game.addBonusesToRewards(activeRoom.getRoomPlayerList(), playerRewards);

        for (FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());

            if (isActivePlayer) {
                gameLogContent.append(isCurrentPlayerInRedTeam ? "red " : "blue ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                final boolean wonGame = isCurrentPlayerInRedTeam && allPlayersTeamBlueDead || !isCurrentPlayerInRedTeam && allPlayersTeamRedDead;

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

                game.addRewardItemToPocket(client, playerReward);

                PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());
                if (wonGame) {
                    playerStatistic.setBattleRecordWin(playerStatistic.getBattleRecordWin() + 1);

                    int newCurrentConsecutiveWins = playerStatistic.getConsecutiveWins() + 1;
                    if (newCurrentConsecutiveWins > playerStatistic.getMaxConsecutiveWins()) {
                        playerStatistic.setMaxConsecutiveWins(newCurrentConsecutiveWins);
                    }

                    playerStatistic.setConsecutiveWins(newCurrentConsecutiveWins);
                } else {
                    playerStatistic.setBattleRecordLoss(playerStatistic.getBattleRecordLoss() + 1);
                    playerStatistic.setConsecutiveWins(0);
                }

                HashMap<Long, Integer> playerRatings = RankingUtils.calculateNewRating(playerList, player, wonGame, (byte) GameMode.BATTLE);
                int playerRankingPoints = playerRatings.get(player.getId()) - playerStatistic.getBattleRP();
                int playerNewRating = playerRatings.get(player.getId());

                playerReward.setRankingPoints(playerRankingPoints);

                playerStatistic.setBattleRP(Math.max(playerNewRating, 0));

                playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

                player.setPlayerStatistic(playerStatistic);
                client.savePlayer(player);

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

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.BATTLE_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
        }
    }

    @Override
    public void onPrepare(FTClient ftClient) {
        Room room = ftClient.getActiveRoom();
        game.setWillDamages(willDamageService.getWillDamages());

        room.getRoomPlayerList().forEach(roomPlayer -> {
            if (roomPlayer.getPosition() < 4)
                game.getPlayerBattleStates().add(game.createPlayerBattleState(roomPlayer));
        });
    }

    @Override
    public void onPoint(FTClient ftClient, C2SMatchplayPointPacket matchplayPointPacket) {
        boolean lastGuardianServeWasOnBlueTeamsSide = game.getLastGuardianServeSide().get() == GameFieldSide.BlueTeam;
        int servingPositionXOffset = random.nextInt(7);

        S2CMatchplayTriggerGuardianServe triggerGuardianServePacket;
        if (!lastGuardianServeWasOnBlueTeamsSide) {
            game.getLastGuardianServeSide().set(GameFieldSide.BlueTeam);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.BlueTeam, (byte) servingPositionXOffset, (byte) 0);
        } else {
            game.getLastGuardianServeSide().set(GameFieldSide.RedTeam);
            triggerGuardianServePacket = new S2CMatchplayTriggerGuardianServe((byte) GameFieldSide.RedTeam, (byte) servingPositionXOffset, (byte) 0);
        }
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(triggerGuardianServePacket, ftClient.getConnection());
    }
}
