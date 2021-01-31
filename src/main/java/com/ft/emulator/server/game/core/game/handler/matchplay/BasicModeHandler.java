package com.ft.emulator.server.game.core.game.handler.matchplay;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.constants.RoomStatus;
import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.PlayerReward;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.game.core.service.LevelService;
import com.ft.emulator.server.game.core.service.PlayerStatisticService;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BasicModeHandler {
    private final LevelService levelService;
    private final PlayerStatisticService playerStatisticService;
    private final PacketEventHandler packetEventHandler;
    private final GameHandler gameHandler;

    public void handleBasicModeMatchplayPointPacket(Connection connection, C2SMatchplayPointPacket matchplayPointPacket, GameSession gameSession, MatchplayBasicGame game) {
        boolean isSingles = gameSession.getPlayers() == 2;
        byte pointsTeamRed = game.getPointsRedTeam();
        byte pointsTeamBlue = game.getPointsBlueTeam();
        byte setsTeamRead = game.getSetsRedTeam();
        byte setsTeamBlue = game.getSetsBlueTeam();

        if (matchplayPointPacket.getPlayerPosition() < 4) {
            game.increasePerformancePointForPlayer(matchplayPointPacket.getPlayerPosition());
        }

        if (game.isRedTeam(matchplayPointPacket.getPointsTeam()))
            game.setPoints((byte) (pointsTeamRed + 1), pointsTeamBlue);
        else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()))
            game.setPoints(pointsTeamRed, (byte) (pointsTeamBlue + 1));

        boolean anyTeamWonSet = setsTeamRead != game.getSetsRedTeam() || setsTeamBlue != game.getSetsBlueTeam();
        if (anyTeamWonSet) {
            gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
            game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
        }

        boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());
        List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();

        game.setFinished(true);
        game.setSetsBlueTeam((byte) 2);
        List<PlayerReward> playerRewards = new ArrayList<>();
        if (game.isFinished()) {
            playerRewards = game.getPlayerRewards();
            connection.getClient().getActiveRoom().setStatus(RoomStatus.NotRunning);
        }

        List<ServeInfo> serveInfo = new ArrayList<>();
        List<Client> clients = gameSession.getClients();
        for (Client client : clients) {
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                    .findFirst().orElse(null);
            if (rp == null) {
                continue;
            }

            boolean isCurrentPlayerInRedTeam = game.isRedTeam(rp.getPosition());
            boolean shouldPlayerSwitchServingSide =
                    game.shouldSwitchServingSide(isSingles, isRedTeamServing, anyTeamWonSet, rp.getPosition());
            if (shouldPlayerSwitchServingSide) {
                Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
                game.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
            }

            if (!game.isFinished()) {
                short pointingTeamPosition = -1;
                if (game.isRedTeam(matchplayPointPacket.getPointsTeam()))
                    pointingTeamPosition = 0;
                else if (game.isBlueTeam(matchplayPointPacket.getPointsTeam()))
                    pointingTeamPosition = 1;

                S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint =
                        new S2CMatchplayTeamWinsPoint(pointingTeamPosition, matchplayPointPacket.getBallState(), game.getPointsRedTeam(), game.getPointsBlueTeam());
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                if (anyTeamWonSet) {
                    S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet(game.getSetsRedTeam(), game.getSetsBlueTeam());
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                }
            }

            if (game.isFinished()) {
                boolean wonGame = false;
                if (isCurrentPlayerInRedTeam && game.getSetsRedTeam() == 2 || !isCurrentPlayerInRedTeam && game.getSetsBlueTeam() == 2) {
                    wonGame = true;
                }

                PlayerReward playerReward = playerRewards.stream()
                        .filter(x -> x.getPlayerPosition() == rp.getPosition())
                        .findFirst()
                        .orElse(null);
                if (playerReward != null) {
                    byte level = levelService.getLevel(playerReward.getBasicRewardExp(), client.getActivePlayer().getExpPoints(), client.getActivePlayer().getLevel());
                    Player player = client.getActivePlayer();
                    player.setExpPoints(player.getExpPoints() + playerReward.getBasicRewardExp());
                    player.setGold(player.getGold() + playerReward.getBasicRewardGold());
                    player = levelService.setNewLevelStatusPoints(level, player);
                    client.setActivePlayer(player);
                }

                Player player = client.getActivePlayer();
                if (wonGame) {
                    player.getPlayerStatistic().setBasicRecordWin(player.getPlayerStatistic().getBasicRecordWin() + 1);

                    int newCurrentConsecutiveWins = player.getPlayerStatistic().getConsecutiveWins() + 1;
                    if (newCurrentConsecutiveWins > player.getPlayerStatistic().getMaxConsecutiveWins()) {
                        player.getPlayerStatistic().setMaxConsecutiveWins(newCurrentConsecutiveWins);
                    }

                    player.getPlayerStatistic().setConsecutiveWins(newCurrentConsecutiveWins);
                    playerStatisticService.save(player.getPlayerStatistic());
                } else {
                    player.getPlayerStatistic().setBasicRecordLoss(player.getPlayerStatistic().getBasicRecordLoss() + 1);
                    player.getPlayerStatistic().setConsecutiveWins(0);
                    playerStatisticService.save(player.getPlayerStatistic());
                }

                rp.setPlayer(player);
                rp.setReady(false);
                byte playerLevel = client.getActivePlayer().getLevel();
                byte resultTitle = (byte) (wonGame ? 1 : 0);
                S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000), playerReward, playerLevel);
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(playerRewards);
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);

                if (rp.getPosition() == 0) {
                    Packet unsetHostPacket = new Packet(PacketID.S2CUnsetHost);
                    unsetHostPacket.write((byte) 0);
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, unsetHostPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
                }
            }
            else {
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
                            .ifPresent(x -> game.setReceiverPlayer(x));
                }
            }

            S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
            for (Client client : clients)
                packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(8)), PacketEventHandler.ServerClient.SERVER);
        }
    }

    public void handleStartBasicMode(Room room) {
        List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        List<Client> clients = this.gameHandler.getClientsInRoom(room.getRoomId());
        List<ServeInfo> serveInfo = new ArrayList<>();
        clients.forEach(c -> {
            RoomPlayer rp = roomPlayerList.stream()
                    .filter(x -> x.getPlayer().getId().equals(c.getActivePlayer().getId()))
                    .findFirst().orElse(null);

            GameSession gameSession = c.getActiveGameSession();
            MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getActiveMatchplayGame();

            Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
            byte serveType = ServeType.None;
            if (rp.getPosition() == 0) {
                serveType = ServeType.ServeBall;

                if (gameSession.getActiveMatchplayGame() instanceof MatchplayBasicGame)
                    ((MatchplayBasicGame) gameSession.getActiveMatchplayGame()).setServePlayer(rp);
            }
            if (rp.getPosition() == 1) {
                serveType = ServeType.ReceiveBall;

                if (gameSession.getActiveMatchplayGame() instanceof MatchplayBasicGame)
                    ((MatchplayBasicGame) gameSession.getActiveMatchplayGame()).setReceiverPlayer(rp);
            }
            ServeInfo playerServeInfo = new ServeInfo();
            playerServeInfo.setPlayerPosition(rp.getPosition());
            playerServeInfo.setPlayerStartLocation(playerLocation);
            playerServeInfo.setServeType(serveType);
            serveInfo.add(playerServeInfo);
        });

        S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
        clients.forEach(c -> {
            c.getConnection().sendTCP(matchplayTriggerServe);
        });
    }
}
