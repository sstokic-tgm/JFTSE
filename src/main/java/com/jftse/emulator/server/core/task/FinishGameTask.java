package com.jftse.emulator.server.core.task;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.item.EItemUseType;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonus;
import com.jftse.emulator.server.core.life.progression.ExpGoldBonusImpl;
import com.jftse.emulator.server.core.life.progression.bonuses.BattleHouseBonus;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.PlayerReward;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.S2CDCMsgPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.*;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.core.thread.AbstractTask;
import com.jftse.emulator.server.core.utils.RankingUtils;
import com.jftse.emulator.server.database.model.log.GameLog;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.database.model.log.GameLogType;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.shared.module.Client;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FinishGameTask extends AbstractTask {
    private final Connection connection;

    private boolean wonGame;

    private final LevelService levelService;
    private final ClothEquipmentService clothEquipmentService;
    private final ProductService productService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;
    private final GameLogService gameLogService;

    private final PacketEventHandler packetEventHandler;

    public FinishGameTask(Connection connection, boolean wonGame) {
        this.connection = connection;
        this.wonGame = wonGame;

        this.levelService = ServiceManager.getInstance().getLevelService();
        this.clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        this.productService = ServiceManager.getInstance().getProductService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();

        packetEventHandler = GameManager.getInstance().getPacketEventHandler();
    }

    public FinishGameTask(Connection connection) {
        this.connection = connection;

        this.levelService = ServiceManager.getInstance().getLevelService();
        this.clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        this.productService = ServiceManager.getInstance().getProductService();
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        this.gameLogService = ServiceManager.getInstance().getGameLogService();

        packetEventHandler = GameManager.getInstance().getPacketEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getActiveMatchplayGame();

        if (game != null && !game.isFinished()) {
            if (game instanceof MatchplayBattleGame) {
                ((MatchplayBattleGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));
                finishBattleGame(connection, (MatchplayBattleGame) game);
            } else if (game instanceof MatchplayGuardianGame) {
                ((MatchplayGuardianGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));
                finishGuardianGame(connection, (MatchplayGuardianGame) game, wonGame);
            }
        }
    }

    private void finishBattleGame(Connection connection, MatchplayBattleGame game) {
        final boolean isFinished = game.isFinished();
        if (isFinished)
            return;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        game.setEndTime(cal.getTime());

        if (game.isFinished() == isFinished)
            game.setFinished(true);

        List<PlayerReward> playerRewards = game.getPlayerRewards();

        Room room = connection.getClient().getActiveRoom();
        synchronized (room) {
            room.setStatus(RoomStatus.NotRunning);
        }

        GameSession gameSession = connection.getClient().getActiveGameSession();
        gameSession.clearCountDownRunnable();
        gameSession.getRunnableEvents().clear();

        List<Player> playerList = new ArrayList<>();
        for (Iterator<Client> it = gameSession.getClients().iterator(); it.hasNext(); )
            playerList.add(it.next().getPlayer());

        addProgressionBonus(playerRewards, gameSession);

        StringBuilder gameLogContent = new StringBuilder();
        gameLogContent.append("Battle game finished. ");

        boolean allPlayersTeamRedDead = game.getPlayerBattleStates().stream().filter(x -> game.isRedTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);
        boolean allPlayersTeamBlueDead = game.getPlayerBattleStates().stream().filter(x -> game.isBlueTeam(x.getPosition())).allMatch(x -> x.getCurrentHealth().get() < 1);

        gameLogContent.append(allPlayersTeamRedDead ? "Blue " : "Red ").append("team won. ");

        gameSession.getClients().forEach(client -> {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null) {
                return;
            }

            boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
                boolean wonGame = isCurrentPlayerInRedTeam && allPlayersTeamBlueDead || !isCurrentPlayerInRedTeam && allPlayersTeamRedDead;

                gameLogContent.append(isCurrentPlayerInRedTeam ? "red " : "blue ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                PlayerReward playerReward = playerRewards.stream()
                        .filter(x -> x.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(this.createEmptyPlayerReward());

                Player player = client.getPlayer();
                byte oldLevel = player.getLevel();
                byte level = levelService.getLevel(playerReward.getRewardExp(), player.getExpPoints(), player.getLevel());
                if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
                    player.setExpPoints(player.getExpPoints() + playerReward.getRewardExp());
                player.setGold(player.getGold() + playerReward.getRewardGold());
                player = levelService.setNewLevelStatusPoints(level, player);
                client.savePlayer(player);

                this.handleRewardItem(client.getConnection(), playerReward);

                PlayerStatistic playerStatistic = player.getPlayerStatistic();
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
                playerReward.setRewardRP(playerRankingPoints);
                playerStatistic.setBattleRP(Math.max(playerNewRating, 0));

                playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

                player.setPlayerStatistic(playerStatistic);
                client.savePlayer(player);

                synchronized (rp) {
                    rp.setReady(false);
                }
                byte playerLevel = player.getLevel();
                if (playerLevel != oldLevel) {
                    StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);

                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }

                S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);

                byte resultTitle = (byte) (wonGame ? 1 : 0);
                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel);
                client.getConnection().sendTCP(setExperienceGainInfoData);
            } else {
                gameLogContent.append("spec: ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
            }

            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
            client.getConnection().sendTCP(setGameResultData);

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            packetEventHandler.push(packetEventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
            client.setActiveGameSession(null);
        });

        gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.BATTLE_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.isFinished() && gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSession);
        }
    }

    private void finishGuardianGame(Connection connection, MatchplayGuardianGame game, boolean wonGame) {
        final boolean isFinished = game.isFinished();
        if (isFinished)
            return;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        game.setEndTime(cal.getTime());

        if (game.isFinished() == isFinished)
            game.setFinished(true);

        Room room = connection.getClient().getActiveRoom();
        synchronized (room) {
            room.setStatus(RoomStatus.NotRunning);
        }

        GameSession gameSession = connection.getClient().getActiveGameSession();
        gameSession.clearCountDownRunnable();
        gameSession.getRunnableEvents().clear();

        if (game.isBossBattleActive() && wonGame) {
            long timeNeededSeconds = TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded());
            List<Integer> stages = Arrays.asList(9, 10, 13, 14);

            boolean underSixty = (timeNeededSeconds < 60) && !stages.contains(game.getCurrentStage().getMapId());
            boolean underNinety = (timeNeededSeconds < 90) && stages.contains(game.getCurrentStage().getMapId());

            if (underNinety || underSixty) {
                StringBuilder gameLogContent = new StringBuilder();
                gameLogContent.append("Boss Guardian finished before ");
                if (underNinety)
                    gameLogContent.append("90s. ");
                if (underSixty)
                    gameLogContent.append("60s. ");
                gameLogContent.append(game.getCurrentStage().getName()).append(" ");

                gameSession.getClients().forEach(client -> {
                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(4);
                    client.getConnection().sendTCP(msgPacket);
                    client.getConnection().close();

                    RoomPlayer rp = client.getRoomPlayer();
                    if (rp == null) {
                        return;
                    }

                    gameLogContent.append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
                });
                gameLogContent.append("playtime: ").append(timeNeededSeconds).append("s");

                GameLog gameLog = new GameLog();
                gameLog.setGameLogType(GameLogType.BANABLE);
                gameLog.setContent(gameLogContent.toString());
                gameLogService.save(gameLog);

                gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
                if (game.isFinished() && gameSession.getClients().isEmpty()) {
                    GameSessionManager.getInstance().removeGameSession(gameSession);
                }
                return;
            }
        }

        List<PlayerReward> playerRewards = game.getPlayerRewards(wonGame);
        playerRewards.forEach(x -> {
            int expMultiplier = game.getGuardianStage().getExpMultiplier();
            x.setRewardExp(x.getRewardExp() * expMultiplier);
        });
        addProgressionBonus(playerRewards, gameSession);

        StringBuilder gameLogContent = new StringBuilder();
        gameLogContent.append(game.getCurrentStage().getName()).append(" ");
        gameLogContent.append(game.isBossBattleActive() ? "Boss " : "Guardian ").append("battle finished. ");
        gameLogContent.append(wonGame ? "Players " : "Guardians ").append("won. ");

        gameSession.getClients().forEach(client -> {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null) {
                return;
            }

            boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                gameLogContent.append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");

                PlayerReward playerReward = playerRewards.stream()
                        .filter(x -> x.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(this.createEmptyPlayerReward());

                Player player = client.getPlayer();
                byte oldLevel = player.getLevel();
                byte level = levelService.getLevel(playerReward.getRewardExp(), player.getExpPoints(), player.getLevel());
                if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (oldLevel < level))
                    player.setExpPoints(player.getExpPoints() + playerReward.getRewardExp());
                player.setGold(player.getGold() + playerReward.getRewardGold());
                player = levelService.setNewLevelStatusPoints(level, player);
                client.savePlayer(player);

                if (wonGame) {
                    this.handleRewardItem(client.getConnection(), playerReward);
                }

                synchronized (rp) {
                    rp.setReady(false);
                }
                byte playerLevel = player.getLevel();
                if (playerLevel != oldLevel) {
                    StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                    rp.setStatusPointsAddedDto(statusPointsAddedDto);

                    S2CGameEndLevelUpPlayerStatsPacket gameEndLevelUpPlayerStatsPacket = new S2CGameEndLevelUpPlayerStatsPacket(rp.getPosition(), player, rp.getStatusPointsAddedDto());
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, gameEndLevelUpPlayerStatsPacket, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }

                S2CMatchplayDisplayItemRewards s2CMatchplayDisplayItemRewards = new S2CMatchplayDisplayItemRewards(playerRewards);
                client.getConnection().sendTCP(s2CMatchplayDisplayItemRewards);

                byte resultTitle = (byte) (wonGame ? 1 : 0);
                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel);
                client.getConnection().sendTCP(setExperienceGainInfoData);
            } else {
                gameLogContent.append("spec: ").append(rp.getPlayer().getName()).append(" acc: ").append(rp.getPlayer().getAccount().getId()).append("; ");
            }

            S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
            client.getConnection().sendTCP(setGameResultData);

            S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
            packetEventHandler.push(packetEventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
            client.setActiveGameSession(null);
        });

        gameLogContent.append("playtime: ").append(TimeUnit.MILLISECONDS.toSeconds(game.getTimeNeeded())).append("s");

        GameLog gameLog = new GameLog();
        gameLog.setGameLogType(GameLogType.GUARDIAN_GAME);
        gameLog.setContent(gameLogContent.toString());
        gameLogService.save(gameLog);

        gameSession.getClients().removeIf(c -> c.getActiveGameSession() == null);
        if (game.isFinished() && gameSession.getClients().isEmpty()) {
            GameSessionManager.getInstance().removeGameSession(gameSession);
        }
    }

    private void addProgressionBonus(List<PlayerReward> playerRewards, GameSession gameSession) {
        gameSession.getClients().forEach(client -> {
            RoomPlayer rp = client.getRoomPlayer();
            if (rp == null) {
                return;
            }

            boolean isActivePlayer = rp.getPosition() < 4;
            if (isActivePlayer) {
                PlayerReward playerReward = playerRewards.stream()
                        .filter(x -> x.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(this.createEmptyPlayerReward());

                // add house bonus
                // TODO: should be moved to getPlayerReward sometime...
                ExpGoldBonus expGoldBonus = new BattleHouseBonus(
                        new ExpGoldBonusImpl(playerReward.getRewardExp(), playerReward.getRewardGold()), client.getAccountId());

                int rewardExp = expGoldBonus.calculateExp();
                int rewardGold = expGoldBonus.calculateGold();
                // TODO: ...because of this
                playerReward.setRewardExp(rewardExp);
                playerReward.setRewardGold(rewardGold);
            }
        });
    }

    private PlayerReward createEmptyPlayerReward() {
        PlayerReward playerReward = new PlayerReward();
        playerReward.setRewardExp(1);
        playerReward.setRewardGold(1);
        return playerReward;
    }

    private void handleRewardItem(Connection connection, PlayerReward playerReward) {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        if (playerReward.getRewardProductIndex() < 0)
            return;

        Product product = productService.findProductByProductItemIndex(playerReward.getRewardProductIndex());
        if (product == null)
            return;

        Player player = connection.getClient().getPlayer();
        Pocket pocket = player.getPocket();
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        boolean existingItem = false;

        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setCategory(product.getCategory());
        playerPocket.setItemIndex(product.getItem0());
        playerPocket.setUseType(product.getUseType());

        // no idea how itemCount can be null here, but ok
        playerPocket.setItemCount((playerPocket.getItemCount() == null ? 0 : playerPocket.getItemCount()) + playerReward.getProductRewardAmount());

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
        connection.getClient().savePlayer(player);

        List<PlayerPocket> playerPocketList = new ArrayList<>();
        playerPocketList.add(playerPocket);

        S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(playerPocketList);
        if (connection.isConnected())
            connection.sendTCP(inventoryDataPacket);
    }
}
