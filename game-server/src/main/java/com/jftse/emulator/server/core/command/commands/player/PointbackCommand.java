package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.life.room.ServeInfo;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.EventHandler;
import com.jftse.emulator.server.core.matchplay.event.PacketEvent;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTeamWinsPoint;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTeamWinsSet;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayTriggerServe;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class PointbackCommand extends AbstractCommand {
    private final EventHandler eventHandler;


    public PointbackCommand() {
        setDescription("vote to reset points to last one");

        eventHandler = GameManager.getInstance().getEventHandler();
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (connection.getClient().getActiveRoom() == null || connection.getClient().getRoomPlayer() == null || connection.getClient().getActiveGameSession() == null)
            return;

        Room activeRoom = connection.getClient().getActiveRoom();
        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession.getMatchplayGame() instanceof MatchplayBasicGame game) {
            final boolean isFinished = game.getFinished().get();

            if (isFinished || (game.getSetsBlueTeam().get() == 0 && game.getSetsRedTeam().get() == 0 && game.getPointsBlueTeam().get() == 0 & game.getPointsRedTeam().get() == 0))
                return;

            boolean isSingles = gameSession.getPlayers() == 2;
            ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = activeRoom.getRoomPlayerList();
            List<ServeInfo> serveInfos = new ArrayList<>();
            List<FTClient> clients = new ArrayList<>(gameSession.getClients());
            boolean setsDownGraded = false;
            boolean pointsBackSuccess = false;
            for (FTClient client : clients) {
                RoomPlayer rp = client.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer) {
                    if (roomPlayer.getPlayerId().equals(rp.getPlayerId())) {
                        game.setPointBackVote(rp.getPosition());
                        S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", rp.getPlayer().getName() + " voted for point back");
                        GameManager.getInstance().getClientsInRoom(activeRoom.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
                    }
                }
            }

            if (game.isPointBackAvailable()) {
                game.pointBack();
                pointsBackSuccess = true;
                if (game.getSetDowngraded().get()) {
                    setsDownGraded = true;
                }
                Optional<PacketEvent> packetEvent = eventHandler.getFireableDeque().stream()
                        .filter(f -> f instanceof PacketEvent)
                        .map(f -> (PacketEvent) f)
                        .filter(pe -> !pe.isFired() && pe.getPacketEventType() == PacketEventType.FIRE_DELAYED && pe.getPacket() instanceof S2CMatchplayTriggerServe)
                        .findFirst();
                packetEvent.ifPresent(eventHandler::remove);
            }

            if (setsDownGraded) {
                gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() - 1);
                game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
            }

            for (FTClient client : clients) {
                RoomPlayer rp = client.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer && pointsBackSuccess) {
                    boolean isRedTeamServing = game.isRedTeamServing(gameSession.getTimesCourtChanged());
                    boolean shouldPlayerSwitchServingSide =
                            game.shouldSwitchServingSide(isSingles, isRedTeamServing, setsDownGraded, rp.getPosition());
                    if (shouldPlayerSwitchServingSide) {
                        Point playerLocation = game.getPlayerLocationsOnMap().get(rp.getPosition());
                        game.getPlayerLocationsOnMap().set(rp.getPosition(), game.invertPointX(playerLocation));
                    }


                    byte serveType = ServeType.None;
                    if (rp.getPosition() == game.getPreviousServePlayerPosition().get()) {
                        serveType = ServeType.ServeBall;
                        game.getServePlayer().set(rp);
                    } else if (rp.getPosition() == game.getPreviousReceiverPlayerPosition().get()) {
                        serveType = ServeType.ReceiveBall;
                        game.getReceiverPlayer().set(rp);
                    }

                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(game.getPlayerLocationsOnMap().get(rp.getPosition()));
                    playerServeInfo.setServeType(serveType);
                    serveInfos.add(playerServeInfo);

                    S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint = new S2CMatchplayTeamWinsPoint((byte) 0, (byte) 0, (byte) game.getPointsRedTeam().get(), (byte) game.getPointsBlueTeam().get());
                    eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0));

                    if (setsDownGraded) {
                        S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet((byte) game.getSetsRedTeam().get(), (byte) game.getSetsBlueTeam().get());
                        eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0));
                    }
                }
            }

            if (serveInfos.size() > 0) {
                if (!isSingles) {
                    game.setPlayerLocationsForDoubles(serveInfos);
                    Optional<ServeInfo> receiver = serveInfos.stream()
                            .filter(x -> x.getServeType() == ServeType.ReceiveBall)
                            .findFirst();
                    receiver.flatMap(serveInfo -> roomPlayerList.stream()
                                    .filter(x -> x.getPosition() == serveInfo.getPlayerPosition())
                                    .findFirst())
                            .ifPresent(rp -> game.getReceiverPlayer().set(rp));
                }

                S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfos);
                for (FTClient client : clients)
                    eventHandler.offer(eventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.FIRE_DELAYED, TimeUnit.SECONDS.toMillis(6)));
            }

            if (pointsBackSuccess) {
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Point back voted successfully.");
                GameManager.getInstance().getClientsInRoom(activeRoom.getRoomId()).forEach(c -> c.getConnection().sendTCP(chatRoomAnswerPacket));
            }
        }
    }
}
