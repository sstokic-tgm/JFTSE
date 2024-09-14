package com.jftse.emulator.server.core.matchplay.handler;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.core.life.item.special.RingOfExp;
import com.jftse.emulator.server.core.life.item.special.RingOfGold;
import com.jftse.emulator.server.core.life.item.special.RingOfWiseman;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayHandleable;
import com.jftse.emulator.server.core.matchplay.MatchplayReward;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.task.AutoItemRewardPickerTask;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class MatchplayBasicModeHandler implements MatchplayHandleable {
    private final MatchplayBasicGame game;
    private final EventHandler eventHandler;
    private final GameLogService gameLogService;
    private final LevelService levelService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;
    private final ClothEquipmentService clothEquipmentService;
    private final ProductService productService;
    private final MapService mapService;

    public MatchplayBasicModeHandler(MatchplayBasicGame game) {
        this.game = game;
        this.eventHandler = GameManager.getInstance().getEventHandler();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();
        this.levelService = ServiceManager.getInstance().getLevelService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        this.productService = ServiceManager.getInstance().getProductService();
        this.mapService = ServiceManager.getInstance().getMapService();
    }

    @Override
    public void onStart(final FTClient ftClient) {
        Packet removeBlackBarsPacket = new Packet(PacketOperations.S2CGameRemoveBlackBars);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(removeBlackBarsPacket, ftClient.getConnection());

        ConcurrentLinkedDeque<FTClient> clients = ftClient.getActiveGameSession().getClients();
        List<ServeInfo> serveInfo = new ArrayList<>();

        for (final FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());

                byte serveType = ServeType.None;
                if (rp.getPosition() == 0) {
                    serveType = ServeType.ServeBall;
                    game.getServePlayer().set(rp);
                }
                if (rp.getPosition() == 1) {
                    serveType = ServeType.ReceiveBall;
                    game.getReceiverPlayer().set(rp);
                }
                ServeInfo playerServeInfo = new ServeInfo();
                playerServeInfo.setPlayerPosition(rp.getPosition());
                playerServeInfo.setPlayerStartLocation(playerLocation);
                playerServeInfo.setServeType(serveType);
                serveInfo.add(playerServeInfo);
            }
        }
        S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(matchplayTriggerServe, ftClient.getConnection());
    }

    @Override
    public void onEnd(final FTClient ftClient) {
        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        final Integer gameSessionId = ftClient.getGameSessionId();

        Room activeRoom = ftClient.getActiveRoom();
        if (activeRoom == null)
            return;

        activeRoom.setStatus(RoomStatus.NotRunning);

        gameSession.getFireables().forEach(f -> f.setCancelled(true));
        gameSession.getFireables().clear();

        StringBuilder gameLogContent = new StringBuilder();

        gameLogContent.append("Basic game finished. ");
        boolean redTeamWon = game.getSetsRedTeam().get() == 2;
        gameLogContent.append(redTeamWon ? "Red " : "Blue ").append("team won. ");

        MatchplayReward matchplayReward = game.getMatchRewards();
        ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();
        final List<Player> playerList = activeRoom.getRoomPlayerList().stream().map(RoomPlayer::getPlayer).collect(Collectors.toList());

        game.addBonusesToRewards(activeRoom.getRoomPlayerList(), matchplayReward.getPlayerRewards());

        GameSessionManager.getInstance().addMatchplayReward(activeRoom.getRoomId(), matchplayReward);

        for (final FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null)
                continue;

            final boolean isActivePlayer = rp.getPosition() < 4;
            final boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
            if (isActivePlayer) {
                gameLogContent.append(isCurrentPlayerInRedTeam ? "red " : "blue ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                boolean wonGame = (isCurrentPlayerInRedTeam && game.getSetsRedTeam().get() == 2) || (!isCurrentPlayerInRedTeam && game.getSetsBlueTeam().get() == 2);

                PlayerReward playerReward = matchplayReward.getPlayerReward(rp.getPosition());
                if (playerReward == null) {
                    playerReward = new PlayerReward(rp.getPosition());
                }

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

                PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());
                if (wonGame) {
                    playerStatistic.setBasicRecordWin(playerStatistic.getBasicRecordWin() + 1);

                    int newCurrentConsecutiveWins = playerStatistic.getConsecutiveWins() + 1;
                    if (newCurrentConsecutiveWins > playerStatistic.getMaxConsecutiveWins()) {
                        playerStatistic.setMaxConsecutiveWins(newCurrentConsecutiveWins);
                    }

                    playerStatistic.setConsecutiveWins(newCurrentConsecutiveWins);
                } else {
                    playerStatistic.setBasicRecordLoss(playerStatistic.getBasicRecordLoss() + 1);
                    playerStatistic.setConsecutiveWins(0);
                }

                HashMap<Long, Integer> playerRatings = RankingUtils.calculateNewRating(playerList, player, wonGame, (byte) GameMode.BASIC);
                int playerRankingPoints = playerRatings.get(player.getId()) - playerStatistic.getBasicRP();
                int playerNewRating = playerRatings.get(player.getId());

                playerReward.setRankingPoints(playerRankingPoints);

                playerStatistic.setBasicRP(Math.max(playerNewRating, 0));

                playerStatistic = playerStatisticService.save(playerStatistic);

                player.setPlayerStatistic(playerStatistic);
                client.savePlayer(player);

                rp.setReady(false);
                byte playerLevel = player.getLevel();
                byte resultTitle = (byte) (wonGame ? 1 : 0);
                if (playerLevel != oldLevel) {
                    StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);

                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                    eventHandler.offer(eventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0));
                }

                S2CMatchplayItemRewardsPacket itemRewardsPacket = new S2CMatchplayItemRewardsPacket(matchplayReward);
                client.getConnection().sendTCP(itemRewardsPacket);

                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel, rp);
                eventHandler.offer(eventHandler.createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0));
            } else {
                gameLogContent.append("spec: ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
            }
            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(matchplayReward.getPlayerRewards());
            eventHandler.offer(eventHandler.createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0));

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            eventHandler.offer(eventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)));
            client.setActiveGameSession(null);
        }

        eventHandler.offer(eventHandler.createRunnableEvent(new AutoItemRewardPickerTask(new ConcurrentLinkedDeque<>(clients), activeRoom.getRoomId()), TimeUnit.SECONDS.toMillis(9)));

        gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.BASIC_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
        }
    }

    @Override
    public void onPrepare(final FTClient ftClient) {
        Room room = ftClient.getActiveRoom();

        Optional<SMaps> map = mapService.findByMap((int) room.getMap());
        if (map.isEmpty()) {
            log.error("No map found for mapId: " + room.getMap());
            return;
        }
        game.setMap(map.get());
    }

    @Override
    public void onPoint(final FTClient ftClient, C2SMatchplayPointPacket matchplayPointPacket) {
        GameSession gameSession = ftClient.getActiveGameSession();
        if (gameSession == null)
            return;

        Room activeRoom = ftClient.getActiveRoom();
        if (activeRoom == null)
            return;

        boolean isSingles = gameSession.getPlayers() == 2;
        final int pointsTeamRed = game.getPointsRedTeam().get();
        final int pointsTeamBlue = game.getPointsBlueTeam().get();
        final int setsTeamRead = game.getSetsRedTeam().get();
        final int setsTeamBlue = game.getSetsBlueTeam().get();

        if (matchplayPointPacket.getPlayerPosition() < 4)
            game.increasePerformancePointForPlayer(matchplayPointPacket.getPlayerPosition());

        if (game.isRedTeam(matchplayPointPacket.getPointsTeam()) && game.isRedTeam(matchplayPointPacket.getPlayerPosition()))
            game.setPoints((byte) (pointsTeamRed + 1), (byte) pointsTeamBlue);
        else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()) && game.isBlueTeam(matchplayPointPacket.getPlayerPosition()))
            game.setPoints((byte) pointsTeamRed, (byte) (pointsTeamBlue + 1));

        final boolean isFinished = game.getFinished().get();

        if (isFinished) {
            this.onEnd(ftClient);
        } else {
            boolean anyTeamWonSet = setsTeamRead != game.getSetsRedTeam().get() || setsTeamBlue != game.getSetsBlueTeam().get();
            if (anyTeamWonSet) {
                gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
                game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
            }
            boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());

            List<ServeInfo> serveInfo = new ArrayList<>();
            ConcurrentLinkedDeque<FTClient> clients = gameSession.getClients();
            ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = activeRoom.getRoomPlayerList();
            for (FTClient client : clients) {
                RoomPlayer rp = client.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer) {
                    boolean shouldPlayerSwitchServingSide = game.shouldSwitchServingSide(isSingles, isRedTeamServing, anyTeamWonSet, rp.getPosition());
                    if (shouldPlayerSwitchServingSide) {
                        Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
                        game.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
                    }
                }

                short pointingTeamPosition = -1;
                if (game.isRedTeam(matchplayPointPacket.getPointsTeam()) && game.isRedTeam(matchplayPointPacket.getPlayerPosition()))
                    pointingTeamPosition = 0;
                else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()) && game.isBlueTeam(matchplayPointPacket.getPlayerPosition()))
                    pointingTeamPosition = 1;

                S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint = new S2CMatchplayTeamWinsPoint(pointingTeamPosition, matchplayPointPacket.getBallState(), (byte) game.getPointsRedTeam().get(), (byte) game.getPointsBlueTeam().get());
                eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0));

                if (anyTeamWonSet) {
                    S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet((byte) game.getSetsRedTeam().get(), (byte) game.getSetsBlueTeam().get());
                    eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0));
                }

                if (isActivePlayer) {
                    boolean shouldServeBall = game.shouldPlayerServe(isSingles, gameSession.getTimesCourtChanged(), rp.getPosition());
                    byte serveType = ServeType.None;

                    if (shouldServeBall) {
                        serveType = ServeType.ServeBall;
                        game.getServePlayer().set(rp);
                    }
                    if (!shouldServeBall && isSingles) {
                        serveType = ServeType.ReceiveBall;
                        game.getReceiverPlayer().set(rp);
                    }

                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(game.getPlayerLocationsOnMap().get(rp.getPosition()));
                    playerServeInfo.setServeType(serveType);
                    serveInfo.add(playerServeInfo);
                }
            }

            if (!serveInfo.isEmpty()) {
                if (!isSingles) {
                    game.setPlayerLocationsForDoubles(serveInfo);
                    serveInfo.stream()
                            .filter(x -> x.getServeType() == ServeType.ReceiveBall)
                            .findFirst()
                            .flatMap(receiver -> roomPlayerList.stream()
                                    .filter(x -> x.getPosition() == receiver.getPlayerPosition())
                                    .findFirst())
                            .ifPresent(x -> game.getReceiverPlayer().set(x));
                }
                S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
                for (FTClient client : clients)
                    eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(6)));
            }
        }
    }
}
