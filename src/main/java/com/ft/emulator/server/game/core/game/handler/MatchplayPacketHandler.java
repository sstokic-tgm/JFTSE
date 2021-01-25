package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.constants.PlayerAnimationType;
import com.ft.emulator.server.game.core.constants.RoomStatus;
import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.GameSessionManager;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEvent;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.*;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CBallAnimationPacket;
import com.ft.emulator.server.game.core.packet.packets.matchplay.relay.C2CPlayerAnimationPacket;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.RelayHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class MatchplayPacketHandler {
    private final GameSessionManager gameSessionManager;
    private final RelayHandler relayHandler;
    private final PacketEventHandler packetEventHandler;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::handleQueuedPackets, 0, 1, TimeUnit.MILLISECONDS);
    }

    public RelayHandler getRelayHandler() {
        return relayHandler;
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleRelayPacketToClientsInGameSessionRequest(Connection connection, Packet packet) {
        Packet relayPacket = new Packet(packet.getData());

        GameSession gameSession = connection.getClient().getActiveGameSession();

        switch (relayPacket.getPacketId()) {
            case PacketID.C2CBallAnimationPacket:
                C2CBallAnimationPacket ballAnimationPacket = new C2CBallAnimationPacket(relayPacket);
                gameSession.setTimeLastBallWasHit(Instant.now().toEpochMilli());
                gameSession.setLastBallHitByPlayer(ballAnimationPacket.getPlayerPosition());

                log.info("Session " + gameSession.getSessionId() + " received ball animation Packet, playerPos " + ballAnimationPacket.getPlayerPosition() + " " + connection.getClient().getActivePlayer().getName());
                packetEventHandler.push(createPacketEvent(connection.getClient(), ballAnimationPacket, PacketEventType.DEFAULT, TimeUnit.SECONDS.toMillis(3)), PacketEventHandler.ServerClient.CLIENT);
                break;
            case PacketID.C2CPlayerAnimationPacket:
                C2CPlayerAnimationPacket playerAnimationPacket = new C2CPlayerAnimationPacket(relayPacket);
                long eventFireTime = 0;
                byte animationType = playerAnimationPacket.getAnimationType();
                if (animationType == PlayerAnimationType.ActivateSkillshot || animationType == PlayerAnimationType.SkillshotOffensive || animationType == PlayerAnimationType.SkillshotDefensive)
                    eventFireTime = TimeUnit.SECONDS.toMillis(5);

                packetEventHandler.push(createPacketEvent(connection.getClient(), playerAnimationPacket, PacketEventType.DEFAULT, eventFireTime), PacketEventHandler.ServerClient.CLIENT);
                break;
        }

        sendPacketToAllClientInSameGameSession(connection, relayPacket);
    }

    public void handleRegisterPlayerForSession(Connection connection, Packet packet) {
        C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);

        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        GameSession gameSession = this.gameSessionManager.getGameSessionBySessionId(sessionId);
        if (gameSession != null) {
            Client playerClient = gameSession.getClientByPlayerId(playerId);
            playerClient.setActiveGameSession(gameSession);
            playerClient.setRelayConnection(connection);
            connection.setClient(playerClient);

            this.relayHandler.addClient(playerClient);

            Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
            answer.write((byte) 0);
            connection.sendTCP(answer);
        }
        else {
            // disconnect all clients maybe? put them back to the room maybe?
        }
    }

    public void handleDisconnect(Connection connection) {
        Client client = connection.getClient();
        if (client == null) return;
        GameSession gameSession = client.getActiveGameSession();
        if (gameSession == null) return;

        client.setActiveGameSession(null);
        gameSession.getClients().removeIf(x -> x.getRelayConnection().getId() == connection.getId());

        Room room = client.getActiveRoom();
        if (room != null) {
            if (room.getStatus() != RoomStatus.NotRunning) {
                S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                gameSession.getClients().forEach(c -> c.getConnection().sendTCP(backToRoomPacket));
            }

            // TODO: Joining player should be able to join running game replacing the disconnected one
            room.setStatus(RoomStatus.NotRunning); // reset status so joining players can join room.
        }

        if (gameSession.getClients().size() == 0) {
            this.gameSessionManager.removeGameSession(gameSession);
        }
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }

    private void sendPacketToAllClientInSameGameSession(Connection connection, Packet packet) {
        List<Client> clientList = relayHandler.getClientsInGameSession(connection.getClient().getActiveGameSession().getSessionId());
        for (Client client : clientList) {
            client.getRelayConnection().sendTCP(packet);
        }
    }

    private void handleGameSessionState(PacketEvent packetEvent, long currentTime) {
        Connection connection = packetEvent.getSender();
        GameSession gameSession = packetEvent.getClient().getActiveGameSession();
        if (gameSession == null) return;
        if (gameSession.getTimeLastBallWasHit() == -1) return;
        if (gameSession.getLastBallHitByPlayer() == -1) return;

        boolean isSingles = gameSession.getPlayers() == 2;

        if (currentTime - gameSession.getTimeLastBallWasHit() > packetEvent.getEventFireTime()) {

            // We need to branch here later for different modes. Best would be without casting haha
            MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getActiveMatchplayGame();

            byte pointsTeamRed = game.getPointsRedTeam();
            byte pointsTeamBlue = game.getPointsBlueTeam();
            byte setsTeamRead = game.getSetsRedTeam();
            byte setsTeamBlue = game.getSetsBlueTeam();

            boolean wrongReceiver = !game.shouldPlayerServe(isSingles, gameSession.getTimesCourtChanged(), gameSession.getLastBallHitByPlayer()) && game.getReceiverPlayer().getPosition() != gameSession.getLastBallHitByPlayer();

            if (game.isRedTeam(gameSession.getLastBallHitByPlayer())) {
                if (wrongReceiver)
                    game.setPoints(game.getPointsRedTeam(), (byte) (game.getPointsBlueTeam() + 1));
                else
                    game.setPoints((byte) (game.getPointsRedTeam() + 1), game.getPointsBlueTeam());
            }
            else if (game.isBlueTeam(gameSession.getLastBallHitByPlayer())) {
                if (wrongReceiver)
                    game.setPoints((byte) (game.getPointsRedTeam() + 1), game.getPointsBlueTeam());
                else
                    game.setPoints(game.getPointsRedTeam(), (byte) (game.getPointsBlueTeam() + 1));
            }
            log.info("Session " + gameSession.getSessionId() + " proceeded point red " + game.getPointsRedTeam() + " blue " + game.getPointsBlueTeam() + " last ball packet playerPos " + gameSession.getLastBallHitByPlayer() + " "
                    + connection.getClient().getActiveRoom().getRoomPlayerList().stream().filter(rp -> rp.getPosition() == gameSession.getLastBallHitByPlayer()).findAny().get().getPlayer().getName());

            boolean anyTeamWonSet = setsTeamRead != game.getSetsRedTeam() || setsTeamBlue != game.getSetsBlueTeam();
            if (anyTeamWonSet) {
                gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
                gameSession.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
            }

            boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());
            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();

            List<ServeInfo> serveInfo = new ArrayList<>();

            List<Client> clients = connection.getClient().getActiveGameSession().getClients();
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
                    Point playerLocation = gameSession.getPlayerLocationsOnMap().get(rp.getPosition());
                    gameSession.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
                }

                if (!game.isFinished()) {
                    short winningPlayerPosition = -1;
                    if (pointsTeamRed != game.getPointsRedTeam())
                        winningPlayerPosition = 0;
                    else if (pointsTeamBlue != game.getPointsBlueTeam())
                        winningPlayerPosition = 1;

                    S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint =
                            new S2CMatchplayTeamWinsPoint(winningPlayerPosition, false, game.getPointsRedTeam(), game.getPointsBlueTeam());
                    packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                    if (anyTeamWonSet) {
                        S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet(game.getSetsRedTeam(), game.getSetsBlueTeam());
                        packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                    }
                }

                if (game.isFinished()) {
                    boolean wonGame = false;
                    if (isCurrentPlayerInRedTeam && game.getSetsRedTeam() == 2 || !isCurrentPlayerInRedTeam && game.getSetsBlueTeam() == 2) {
                        wonGame = true;
                    }

                    rp.setReady(false);
                    byte resultTitle = (byte) (wonGame ? 1 : 0);
                    S2CMatchplaySetExperienceGainInfoData setExperienceGainInfoData = new S2CMatchplaySetExperienceGainInfoData(resultTitle, (int) Math.ceil((double) game.getTimeNeeded() / 1000));
                    packetEventHandler.push(createPacketEvent(client, setExperienceGainInfoData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                    // TODO Order players by performance
                    S2CMatchplaySetGameResultData setGameResultData = new S2CMatchplaySetGameResultData(new int[] { 0, 1 });
                    packetEventHandler.push(createPacketEvent(client, setGameResultData, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                    S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                    packetEventHandler.push(createPacketEvent(client, backToRoomPacket, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(12)), PacketEventHandler.ServerClient.SERVER);
                }
                else {
                    boolean isInServingTeam = isRedTeamServing && game.isRedTeam(rp.getPosition()) || !isRedTeamServing && game.isBlueTeam(rp.getPosition());
                    boolean shouldServeBall = game.shouldPlayerServe(isSingles, gameSession.getTimesCourtChanged(), rp.getPosition());
                    Point startingLocation = game.getStartingLocation(isSingles, isInServingTeam, shouldServeBall, gameSession.getPlayerLocationsOnMap(), rp.getPosition());
                    byte serveType = game.getServeType(shouldServeBall, isInServingTeam, startingLocation);
                    if (serveType == ServeType.ServeBall)
                        game.setServePlayer(rp);
                    else if (serveType == ServeType.ReceiveBall)
                        game.setReceiverPlayer(rp);

                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(startingLocation);
                    playerServeInfo.setServeType(serveType);
                    serveInfo.add(playerServeInfo);
                }
            }

            if (serveInfo.size() > 0) {
                S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfo);
                for (Client client : clients)
                    packetEventHandler.push(createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(8)), PacketEventHandler.ServerClient.SERVER);
            }

            gameSession.setTimeLastBallWasHit(-1);
            gameSession.setLastBallHitByPlayer(-1);
        }
    }

    private void handleQueuedPackets() {
        long currentTime = Instant.now().toEpochMilli();

        // handle client packets in queue
        // pick last occurring event for the ball animation packet
        PacketEvent clientPacketEventBall = packetEventHandler.getClient_packetEventList().stream()
                .filter(packetEvent -> packetEvent.getPacket().getPacketId() == PacketID.C2CBallAnimationPacket && packetEvent.shouldFire(currentTime))
                .reduce((first, second) -> second)
                .orElse(null);
        PacketEvent clientPacketEventPlayer = packetEventHandler.getClient_packetEventList().stream()
                .filter(packetEvent -> {
                    C2CPlayerAnimationPacket playerAnimationPacket = new C2CPlayerAnimationPacket(packetEvent.getPacket());
                    byte animationType = playerAnimationPacket.getAnimationType();

                    if ((playerAnimationPacket.getPacketId() == PacketID.C2CPlayerAnimationPacket) &&
                            (animationType == PlayerAnimationType.ActivateSkillshot || animationType == PlayerAnimationType.SkillshotOffensive ||
                                    animationType == PlayerAnimationType.SkillshotDefensive))
                        return true;
                    else
                        return false;
                })
                .reduce((first, second) -> second)
                .orElse(null);

        if (clientPacketEventBall != null) {
            if (clientPacketEventPlayer == null) {
                handleGameSessionState(clientPacketEventBall, currentTime);
                int index = packetEventHandler.getClient_packetEventList().indexOf(clientPacketEventBall);

                // clear all client packets from the event list up to the last index
                for (int i = index; i >= 0; i--) {
                    packetEventHandler.remove(i, PacketEventHandler.ServerClient.CLIENT);
                    --index;
                }
            }
            else {
                if (clientPacketEventPlayer.shouldFire(currentTime)) {
                    handleGameSessionState(clientPacketEventPlayer, currentTime);
                    int index = packetEventHandler.getClient_packetEventList().indexOf(clientPacketEventPlayer);

                    // clear all client packets from the event list up to the last index
                    for (int i = index; i >= 0; i--) {
                        packetEventHandler.remove(i, PacketEventHandler.ServerClient.CLIENT);
                        --index;
                    }
                }
            }
        }

        // handle server packets in queue
        for (int i = 0; i < packetEventHandler.getServer_packetEventList().size(); i++) {
            PacketEvent packetEvent = packetEventHandler.getServer_packetEventList().get(i);
            if (!packetEvent.isFired() && packetEvent.shouldFire(currentTime)) {
                packetEvent.fire();
                packetEventHandler.remove(i, PacketEventHandler.ServerClient.SERVER);
            }
        }
    }

    private PacketEvent createPacketEvent(Client client, Packet packet, PacketEventType packetEventType, long eventFireTime) {
        long packetTimestamp = Instant.now().toEpochMilli();

        PacketEvent packetEvent = new PacketEvent();
        packetEvent.setSender(client.getConnection());
        packetEvent.setClient(client);
        packetEvent.setPacket(packet);
        packetEvent.setPacketTimestamp(packetTimestamp);
        packetEvent.setPacketEventType(packetEventType);
        packetEvent.setEventFireTime(eventFireTime);

        return packetEvent;
    }

    private static Rectangle getGameFieldRectangle() {
        return new Rectangle(new Point(-6300, -12500), new Dimension(12600,25000));
    }
}