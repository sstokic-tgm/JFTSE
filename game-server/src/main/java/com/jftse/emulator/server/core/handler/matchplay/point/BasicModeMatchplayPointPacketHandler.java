package com.jftse.emulator.server.core.handler.matchplay.point;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.item.special.RingOfExp;
import com.jftse.emulator.server.core.life.item.special.RingOfGold;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.matchplay.*;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.server.core.constants.GameMode;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.bonuses.BasicHouseBonus;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.log.GameLog;
import com.jftse.entities.database.model.log.GameLogType;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.service.*;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Log4j2

public class BasicModeMatchplayPointPacketHandler extends AbstractPacketHandler {
    private C2SMatchplayPointPacket matchplayPointPacket;

    private final PacketEventHandler packetEventHandler;

    private final LevelService levelService;
    private final PlayerStatisticService playerStatisticService;
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final ProductService productService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final GameLogService gameLogService;

    public BasicModeMatchplayPointPacketHandler() {
        packetEventHandler = GameManager.getInstance().getPacketEventHandler();

        levelService = ServiceManager.getInstance().getLevelService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        productService = ServiceManager.getInstance().getProductService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        gameLogService = ServiceManager.getInstance().getGameLogService();
    }

    @Override
    public boolean process(Packet packet) {
        matchplayPointPacket = new C2SMatchplayPointPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        GameSession gameSession = ftClient.getActiveGameSession();
        MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getMatchplayGame();
        int gameSessionId = ftClient.getGameSessionId();

        boolean isSingles = gameSession.getPlayers() == 2;
        int pointsTeamRed = game.getPointsRedTeam();
        int pointsTeamBlue = game.getPointsBlueTeam();
        int setsTeamRead = game.getSetsRedTeam();
        int setsTeamBlue = game.getSetsBlueTeam();

        if (matchplayPointPacket.getPlayerPosition() < 4) {
            game.increasePerformancePointForPlayer(matchplayPointPacket.getPlayerPosition());
        }

        if (game.isRedTeam(matchplayPointPacket.getPointsTeam()))
            game.setPoints((byte) (pointsTeamRed + 1), (byte) pointsTeamBlue);
        else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()))
            game.setPoints((byte) pointsTeamRed, (byte) (pointsTeamBlue + 1));

        final boolean isFinished = game.isFinished();

        boolean anyTeamWonSet = setsTeamRead != game.getSetsRedTeam() || setsTeamBlue != game.getSetsBlueTeam();
        if (anyTeamWonSet) {
            gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
            game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
        }

        boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());
        ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = ftClient.getActiveRoom().getRoomPlayerList();

        List<PlayerReward> playerRewards = new ArrayList<>();
        if (isFinished) {
            playerRewards = game.getPlayerRewards();
            ftClient.getActiveRoom().setStatus(RoomStatus.NotRunning);
        }

        List<ServeInfo> serveInfo = new ArrayList<>();
        List<FTClient> clients = new ArrayList<>(gameSession.getClients());
        List<Player> playerList = new ArrayList<>();
        clients.forEach(c -> playerList.add(c.getPlayer()));

        StringBuilder gameLogContent = new StringBuilder();
        if (isFinished) {
            gameLogContent.append("Basic game finished. ");

            boolean redTeamWon = game.getSetsRedTeam() == 2;
            gameLogContent.append(redTeamWon ? "Red " : "Blue ").append("team won. ");
        }

        for (FTClient client : clients) {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null) {
                continue;
            }

            boolean isActivePlayer = rp.getPosition() < 4;
            boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
            if (isActivePlayer) {
                gameLogContent.append(isCurrentPlayerInRedTeam ? "red " : "blue ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                boolean shouldPlayerSwitchServingSide =
                        game.shouldSwitchServingSide(isSingles, isRedTeamServing, anyTeamWonSet, rp.getPosition());
                if (shouldPlayerSwitchServingSide) {
                    Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
                    game.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
                }
            } else {
                gameLogContent.append("spec: ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
            }

            if (!isFinished) {
                short pointingTeamPosition = -1;
                if (game.isRedTeam(matchplayPointPacket.getPointsTeam()))
                    pointingTeamPosition = 0;
                else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()))
                    pointingTeamPosition = 1;

                S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint =
                        new S2CMatchplayTeamWinsPoint(pointingTeamPosition, matchplayPointPacket.getBallState(), (byte) game.getPointsRedTeam(), (byte) game.getPointsBlueTeam());
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                if (anyTeamWonSet) {
                    S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet((byte) game.getSetsRedTeam(), (byte) game.getSetsBlueTeam());
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }
            }

            if (isFinished) {
                if (isActivePlayer) {
                    boolean wonGame = false;
                    if (isCurrentPlayerInRedTeam && game.getSetsRedTeam() == 2 || !isCurrentPlayerInRedTeam && game.getSetsBlueTeam() == 2) {
                        wonGame = true;
                    }

                    PlayerReward playerReward = playerRewards.stream()
                            .filter(x -> x.getPlayerPosition() == rp.getPosition())
                            .findFirst()
                            .orElse(null);

                    Player player = client.getPlayer();
                    byte oldLevel = player.getLevel();
                    if (playerReward != null) {
                        // add house bonus
                        // TODO: should be moved to getPlayerReward sometime
                        ExpGoldBonus expGoldBonus = new BasicHouseBonus(
                                new ExpGoldBonusImpl(playerReward.getRewardExp(), playerReward.getRewardGold()), client.getAccountId());

                        int rewardExp = expGoldBonus.calculateExp();
                        int rewardGold = expGoldBonus.calculateGold();

                        log.info("EXP/Gold Ring Bonus trying to detect");
                        // Add EXP, Gold Ring Bonus if equipped
                        ItemSpecial specialItemROEXP = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(1);
                        if (handleSpecialWearItem(client.getConnection(), specialItemROEXP)) {
                            log.info("Setting Reward EXP multiplied to 2, before: " + rewardExp);
                            // rewardExp *= 2;
                            log.info("Reward EXP is now: " + rewardExp);
                        }

                        ItemSpecial specialItemROGold = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(2);
                        if (handleSpecialWearItem(client.getConnection(), specialItemROGold)) {
                            log.info("Setting Reward Gold multiplied to 2, before: " + rewardGold);
                            // rewardGold *= 2;
                            log.info("Reward Gold is now: " + rewardGold);
                        }

                        byte level = levelService.getLevel(rewardExp, player.getExpPoints(), player.getLevel());
                        if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
                            player.setExpPoints(player.getExpPoints() + rewardExp);
                        player.setGold(player.getGold() + rewardGold);
                        player = levelService.setNewLevelStatusPoints(level, player);

                        Friend friend = rp.getCouple();
                        if (friend != null) {
                            boolean hasCoupleInTeam = roomPlayerList.stream().anyMatch(roomPlayer -> {
                                boolean isInRedTeam = game.isRedTeam(roomPlayer.getPosition());
                                boolean isInBlueTeam = game.isBlueTeam(roomPlayer.getPosition());
                                if (roomPlayer.getPosition() != rp.getPosition() && (isInRedTeam == isCurrentPlayerInRedTeam || isInBlueTeam == !isCurrentPlayerInRedTeam)) {
                                    Friend f = roomPlayer.getCouple();
                                    return f != null && f.getFriend().getId().equals(friend.getPlayer().getId()) && f.getEFriendshipState() == friend.getEFriendshipState();
                                }
                                return false;
                            });

                            if (hasCoupleInTeam) {
                                int newCouplePoints;
                                if (wonGame)
                                    newCouplePoints = player.getCouplePoints() + 5;
                                else
                                    newCouplePoints = player.getCouplePoints() + 2;

                                player.setCouplePoints(newCouplePoints);
                            }
                        }

                        client.savePlayer(player);

                        this.handleRewardItem(client.getConnection(), playerReward);
                    }

                    PlayerStatistic playerStatistic = player.getPlayerStatistic();
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
                    if (playerReward != null)
                        playerReward.setRewardRP(playerRankingPoints);
                    playerStatistic.setBasicRP(Math.max(playerNewRating, 0));

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
                        packetEventHandler.push(packetEventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                    }

                    S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                    client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);

                    S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel);
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }

                S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
                client.setActiveGameSession(null);
            } else {
                if (isActivePlayer) {
                    boolean shouldServeBall = game.shouldPlayerServe(isSingles, gameSession.getTimesCourtChanged(), rp.getPosition());
                    byte serveType = ServeType.None;
                    if (shouldServeBall) {
                        serveType = ServeType.ServeBall;
                        game.setServePlayer(rp);
                    }

                    if (!shouldServeBall && isSingles) {
                        serveType = ServeType.ReceiveBall;
                        game.setReceiverPlayer(rp);
                    }

                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(game.getPlayerLocationsOnMap().get(rp.getPosition()));
                    playerServeInfo.setServeType(serveType);
                    serveInfo.add(playerServeInfo);
                }
            }
        }

        if (isFinished) {
            gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

            GameLog gameLog = new GameLog();
            gameLog.setGameLogType(GameLogType.BASIC_GAME);
            gameLog.setContent(gameLogContent.toString());
            gameLogService.save(gameLog);
        }

        if (serveInfo.size() > 0) {
            if (!isSingles) {
                game.setPlayerLocationsForDoubles(serveInfo);
                ServeInfo receiver = serveInfo.stream()
                        .filter(x -> x.getServeType() == ServeType.ReceiveBall)
                        .findFirst()
                        .orElse(null);
                if (receiver != null) {
                    roomPlayerList.stream()
                            .filter(x -> x.getPosition() == receiver.getPlayerPosition())
                            .findFirst()
                            .ifPresent(game::setReceiverPlayer);
                }
            }

            S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
            for (FTClient client : clients)
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(6)), PacketEventHandler.ServerClient.SERVER);
        }

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (isFinished && gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSessionId, gameSession);
        }
    }

    private void handleRewardItem(FTConnection connection, PlayerReward playerReward) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        if (playerReward.getRewardProductIndex() < 0)
            return;

        Product product = productService.findProductByProductItemIndex(playerReward.getRewardProductIndex());
        if (product == null)
            return;

        Player player = ftClient.getPlayer();
        Pocket pocket = player.getPocket();
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        int existingItemCount = 0;
        boolean existingItem = false;

        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItemCount = playerPocket.getItemCount();
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setCategory(product.getCategory());
        playerPocket.setItemIndex(product.getItem0());
        playerPocket.setUseType(product.getUseType());

        playerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());

        // no idea how itemCount can be null here, but ok
        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + existingItemCount);

        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

            playerPocket.setCreated(cal.getTime());
        }
        playerPocket.setPocket(pocket);

        playerPocketService.save(playerPocket);
        if (!existingItem)
            pocket = pocketService.incrementPocketBelongings(pocket);

        player.setPocket(pocket);
        ftClient.savePlayer(player);

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        playerPocketList.add(playerPocket);

        S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
        connection.sendTCP(inventoryDataPacket);
    }

    private boolean handleSpecialWearItem(FTConnection connection, ItemSpecial specialItem) {
        ItemSpecial specialItemROEXP = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(1);
        ItemSpecial specialItemROGold = ServiceManager.getInstance().getItemSpecialService().findByItemIndex(2);

        Player player = connection.getClient().getPlayer();
        Pocket playerPocket = player.getPocket();

        log.info(specialItem.getName() + " equals? " + specialItemROEXP.getName());
        if (specialItem.getName().equals(specialItemROEXP.getName())) {

            log.info("Trying to detect special item Ring of EXP");
            RingOfExp ringOfExp = new RingOfExp(specialItemROEXP.getItemIndex(), specialItemROEXP.getName(), "SPECIAL");
            if (ringOfExp.processPlayer(player)) {
                if (ringOfExp.processPocket(playerPocket)) {
                    connection.getClient().savePlayer(player);

                    ringOfExp.getPacketsToSend().forEach((playerId, packets) -> {
                        final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
                        if (connectionByPlayerId != null)
                            connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
                    });

                    List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(playerPocket);
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
                    connection.sendTCP(inventoryDataPacket);
                    return true;
                }
                return false;
            }
            return false;
        } else if (specialItem.getName().equals(specialItemROGold.getName())) {
            log.info("Trying to detect special item Ring of Gold");
            RingOfGold ringOfGold = new RingOfGold(specialItemROGold.getItemIndex(), specialItemROGold.getName(), "SPECIAL");
            if (ringOfGold.processPlayer(player)) {
                if (ringOfGold.processPocket(playerPocket)) {
                    connection.getClient().savePlayer(player);

                    ringOfGold.getPacketsToSend().forEach((playerId, packets) -> {
                        final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(playerId);
                        if (connectionByPlayerId != null)
                            connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
                    });

                    List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(playerPocket);
                    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
                    connection.sendTCP(inventoryDataPacket);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
