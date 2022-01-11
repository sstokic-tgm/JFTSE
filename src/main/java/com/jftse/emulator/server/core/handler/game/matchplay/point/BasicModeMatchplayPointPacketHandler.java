package com.jftse.emulator.server.core.handler.game.matchplay.point;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.item.EItemUseType;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BasicModeMatchplayPointPacketHandler extends AbstractHandler {
    private C2SMatchplayPointPacket matchplayPointPacket;

    private final PacketEventHandler packetEventHandler;

    private final PlayerService playerService;
    private final LevelService levelService;
    private final PlayerStatisticService playerStatisticService;
    private final ClothEquipmentService clothEquipmentService;
    private final ProductService productService;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;

    public BasicModeMatchplayPointPacketHandler() {
        packetEventHandler = GameManager.getInstance().getPacketEventHandler();

        playerService = ServiceManager.getInstance().getPlayerService();
        levelService = ServiceManager.getInstance().getLevelService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        productService = ServiceManager.getInstance().getProductService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();;
    }

    @Override
    public boolean process(Packet packet) {
        matchplayPointPacket = new C2SMatchplayPointPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getActiveMatchplayGame();

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

        boolean anyTeamWonSet = setsTeamRead != game.getSetsRedTeam() || setsTeamBlue != game.getSetsBlueTeam();
        if (anyTeamWonSet) {
            gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
            game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
        }

        boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());
        List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();

        List<PlayerReward> playerRewards = new ArrayList<>();
        if (game.isFinished()) {
            playerRewards = game.getPlayerRewards();
            connection.getClient().getActiveRoom().setStatus(RoomStatus.NotRunning);
        }

        List<ServeInfo> serveInfo = new ArrayList<>();
        List<Client> clients = new ArrayList<>(gameSession.getClients());
        List<Player> playerList = new ArrayList<>();
        clients.forEach(c -> playerList.add(c.getActivePlayer()));

        for (Client client : clients) {
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                    .findFirst().orElse(null);
            if (rp == null) {
                continue;
            }

            boolean isActivePlayer = rp.getPosition() < 4;
            boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
            if (isActivePlayer) {
                boolean shouldPlayerSwitchServingSide =
                        game.shouldSwitchServingSide(isSingles, isRedTeamServing, anyTeamWonSet, rp.getPosition());
                if (shouldPlayerSwitchServingSide) {
                    Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
                    game.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
                }
            }

            if (!game.isFinished()) {
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

            if (game.isFinished()) {
                if (isActivePlayer) {
                    boolean wonGame = false;
                    if (isCurrentPlayerInRedTeam && game.getSetsRedTeam() == 2 || !isCurrentPlayerInRedTeam && game.getSetsBlueTeam() == 2) {
                        wonGame = true;
                    }

                    PlayerReward playerReward = playerRewards.stream()
                            .filter(x -> x.getPlayerPosition() == rp.getPosition())
                            .findFirst()
                            .orElse(null);

                    Player player = client.getActivePlayer();
                    byte oldLevel = player.getLevel();
                    if (playerReward != null) {
                        byte level = levelService.getLevel(playerReward.getRewardExp(), player.getExpPoints(), player.getLevel());
                        if (level != 60)
                            player.setExpPoints(player.getExpPoints() + playerReward.getRewardExp());
                        player.setGold(player.getGold() + playerReward.getRewardGold());
                        player = levelService.setNewLevelStatusPoints(level, player);
                        client.setActivePlayer(player);

                        if (wonGame) {
                            this.handleRewardItem(client.getConnection(), playerReward);
                        }
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
                    playerStatistic.setBasicRP(playerNewRating <= 0 ? 0 : playerNewRating);

                    playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

                    player.setPlayerStatistic(playerStatistic);
                    player = playerService.save(player);
                    client.setActivePlayer(player);

                    rp.setPlayer(player);
                    rp.setReady(false);
                    byte playerLevel = client.getActivePlayer().getLevel();
                    byte resultTitle = (byte) (wonGame ? 1 : 0);
                    if (playerLevel != oldLevel) {
                        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                        rp.setStatusPointsAddedDto(statusPointsAddedDto);

                        S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                        packetEventHandler.push(packetEventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                    }

                    if (wonGame) {
                        S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                        client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);
                    }

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
            for (Client client : clients)
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(6)), PacketEventHandler.ServerClient.SERVER);
        }

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.isFinished() && gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSession);
        }
    }

    private void handleRewardItem(Connection connection, PlayerReward playerReward) {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        if (playerReward.getRewardProductIndex() < 0)
            return;

        Product product = productService.findProductByProductItemIndex(playerReward.getRewardProductIndex());
        if (product == null)
            return;

        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
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
        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        playerPocketList.add(playerPocket);

        S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
        if (connection.isConnected())
            connection.sendTCP(inventoryDataPacket);
    }
}
