package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.constants.PlayerAnimationType;
import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.GameSessionManager;
import com.ft.emulator.server.game.core.matchplay.basic.MatchplayBasicSingleGame;
import com.ft.emulator.server.game.core.matchplay.event.PacketEvent;
import com.ft.emulator.server.game.core.matchplay.event.PacketEventHandler;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
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

        if (currentTime - gameSession.getTimeLastBallWasHit() > packetEvent.getEventFireTime()) {

            // We need to branch here later for different modes. Best would be without casting haha
            MatchplayBasicSingleGame game = (MatchplayBasicSingleGame) gameSession.getActiveMatchplayGame();

            byte setsTeamRead = game.getSetsPlayer1();
            byte setsTeamBlue = game.getSetsPlayer2();

            if (isRedTeam(gameSession.getLastBallHitByPlayer())) {
                game.setPoints((byte) (game.getPointsPlayer1() + 1), game.getPointsPlayer2());
            }
            else if (isBlueTeam(gameSession.getLastBallHitByPlayer())) {
                game.setPoints(game.getPointsPlayer1(), (byte) (game.getPointsPlayer2() + 1));
            }

            boolean anyTeamWonSet = setsTeamRead != game.getSetsPlayer1() || setsTeamBlue != game.getSetsPlayer2();
            if (anyTeamWonSet) {
                gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() + 1);
                gameSession.getPlayerLocationsOnMap().forEach(x -> x.setLocation(this.invertPointY(x)));
            }

            boolean isRedTeamServing = this.isRedTeamServing(gameSession.getTimesCourtChanged());
            List<RoomPlayer> roomPlayerList = connection.getClient().getActiveRoom().getRoomPlayerList();
            int amountActivePlayers = (int) roomPlayerList.stream().filter(x -> x.getPosition() < 4).count();
            boolean isSingles = amountActivePlayers == 2;

            List<ServeInfo> serveInfo = new ArrayList<>();

            List<Client> clients = connection.getClient().getActiveGameSession().getClients();
            for (Client client : clients) {
                RoomPlayer rp = roomPlayerList.stream()
                        .filter(x -> x.getPlayer().getId().equals(client.getActivePlayer().getId()))
                        .findFirst().orElse(null);
                if (rp == null) {
                    continue;
                }

                boolean isCurrentPlayerInRedTeam = isRedTeam(rp.getPosition());
                boolean shouldPlayerSwitchServingSide =
                        shouldSwitchServingSide(isSingles, isRedTeamServing, anyTeamWonSet, rp.getPosition());
                if (shouldPlayerSwitchServingSide) {
                    Point playerLocation = gameSession.getPlayerLocationsOnMap().get(rp.getPosition());
                    gameSession.getPlayerLocationsOnMap().set(rp.getPosition(), this.invertPointX(playerLocation));
                }

                if (!game.isFinished()) {
                    short winningPlayerPosition = (short) (isRedTeam(gameSession.getLastBallHitByPlayer()) ? 0 : 1);
                    S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint =
                            new S2CMatchplayTeamWinsPoint(winningPlayerPosition, false, game.getPointsPlayer1(), game.getPointsPlayer2());
                    packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                    if (anyTeamWonSet) {
                        S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet(game.getSetsPlayer1(), game.getSetsPlayer2());
                        packetEventHandler.push(createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                    }
                }

                if (game.isFinished()) {
                    boolean wonGame = false;
                    if (isCurrentPlayerInRedTeam && game.getSetsPlayer1() == 2 || !isCurrentPlayerInRedTeam && game.getSetsPlayer2() == 2) {
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
                    boolean isInServingTeam = isRedTeamServing && isRedTeam(rp.getPosition()) || !isRedTeamServing && isBlueTeam(rp.getPosition());
                    boolean shouldServeBall = this.shouldPlayerServe(isSingles, gameSession.getTimesCourtChanged(), rp.getPosition());
                    Point startingLocation = this.getStartingLocation(isSingles, isInServingTeam, shouldServeBall, gameSession, rp.getPosition());
                    byte serveType = this.getServeType(shouldServeBall, isInServingTeam, startingLocation);
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

    private byte getServeType(boolean willServeBall, boolean isInServingTeam, Point playerLocation) {
        if (willServeBall) {
            return ServeType.ServeBall;
        }

        if (!isInServingTeam && Math.abs(playerLocation.y) == 125) {
            return ServeType.ReceiveBall;
        }

        return ServeType.None;
    }

    private Point getStartingLocation(boolean isSingles, boolean isInServingTeam, boolean willServeBall, GameSession gameSession, int playerPosition) {
        Point playerLocation = gameSession.getPlayerLocationsOnMap().get(playerPosition);
        if (isSingles) {
            return playerLocation;
        }

        long servingPosY = 125;
        long nonServingPosY = 75;
        long posY = playerLocation.y;
        if (!isInServingTeam) {
            if (playerLocation.y == servingPosY) {
                posY = nonServingPosY;
            }
            if (playerLocation.y == -servingPosY) {
                posY = -nonServingPosY;
            }
            if (playerLocation.y == nonServingPosY) {
                posY = servingPosY;
            }
            if (playerLocation.y == -nonServingPosY) {
                posY = -servingPosY;
            }

            playerLocation.setLocation(playerLocation.x, posY);
            return playerLocation;
        }

        if (willServeBall) {
            posY = playerLocation.y > 0 ? servingPosY : -servingPosY;
        } else {
            posY = playerLocation.y > 0 ? nonServingPosY : -nonServingPosY;
        }

        playerLocation.setLocation(playerLocation.x, posY);
        return playerLocation;
    }

    private Point invertPointX(Point point) {
        return new Point(point.x * (-1), point.y);
    }

    private Point invertPointY(Point point) {
        return new Point(point.x, point.y  * (-1));
    }

    private boolean isRedTeam(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    private boolean isBlueTeam(int playerPos) {
        return playerPos == 1 || playerPos == 3;
    }

    private boolean shouldSwitchServingSide(boolean isSingles, boolean isRedTeamServing, boolean anyTeamWonSet, int playerPosition) {
        if (anyTeamWonSet) {
            return false;
        }

        if (isSingles) {
            return true;
        }

        if (isRedTeamServing && isRedTeam(playerPosition) || !isRedTeamServing && isBlueTeam(playerPosition)) {
            return true;
        }

        return false;
    }

    private boolean isRedTeamServing(int timesCourtChanged) {
        if (this.isEven(timesCourtChanged)) {
            return true;
        }

        return false;
    }

    private boolean shouldPlayerServe(boolean isSingles, int timesCourtChanged, int playerPosition) {
        if (isSingles) {
            if (this.isEven(playerPosition) && this.isEven(timesCourtChanged)) {
                return true;
            }

            if (!this.isEven(playerPosition) && !this.isEven(timesCourtChanged)) {
                return true;
            }
        } else {
            if (playerPosition == timesCourtChanged) {
                return true;
            }
        }

        return false;
    }

    private boolean isEven(int number) {
        return number % 2 == 0;
    }

    private static Rectangle getGameFieldRectangle() {
        return new Rectangle(new Point(-6300, -12500), new Dimension(12600,25000));
    }
}