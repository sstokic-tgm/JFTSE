package com.jftse.emulator.server.core.command.commands.player;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.core.constants.ServeType;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.event.PacketEvent;
import com.jftse.emulator.server.core.matchplay.event.PacketEventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBasicGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTeamWinsPoint;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTeamWinsSet;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayTriggerServe;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.shared.module.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PointbackCommand extends Command {
    private final PacketEventHandler packetEventHandler;


    public PointbackCommand() {
        setDescription("vote to reset points to last one");

        packetEventHandler = GameManager.getInstance().getPacketEventHandler();
    }

    @Override
    public void execute(Connection connection, List<String> params) {
        if (connection.getClient().getActiveRoom() == null || connection.getClient().getRoomPlayer() == null || connection.getClient().getActiveGameSession() == null)
            return;

        Room activeRoom = connection.getClient().getActiveRoom();
        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession.getActiveMatchplayGame() instanceof MatchplayBasicGame) {
            MatchplayBasicGame game = (MatchplayBasicGame) gameSession.getActiveMatchplayGame();
            final boolean isFinished = game.isFinished();

            if (isFinished || (game.getSetsBlueTeam() == 0 && game.getSetsRedTeam() == 0 && game.getPointsBlueTeam() == 0 & game.getPointsRedTeam() == 0))
                return;

            boolean isSingles = gameSession.getPlayers() == 2;
            ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = activeRoom.getRoomPlayerList();
            List<ServeInfo> serveInfos = new ArrayList<>();
            List<Client> clients = new ArrayList<>(gameSession.getClients());
            boolean setsDownGraded = false;
            boolean pointsBackSuccess = false;
            for (Client client : clients) {
                RoomPlayer rp = client.getRoomPlayer();
                if (rp == null)
                    continue;

                boolean isActivePlayer = rp.getPosition() < 4;
                if (isActivePlayer) {
                    if (roomPlayer.getPlayerId().equals(rp.getPlayerId())) {
                        game.setPointBackVote(rp.getPosition());
                        S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", rp.getPlayer().getName() + " voted for point back");
                        GameManager.getInstance().getClientsInRoom(activeRoom.getRoomId()).forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), chatRoomAnswerPacket));
                    }
                }
            }

            if (game.isPointBackAvailable()) {
                game.pointBack();
                pointsBackSuccess = true;
                if (game.isSetDowngraded()) {
                    setsDownGraded = true;
                }
                Optional<PacketEvent> packetEvent = packetEventHandler.getServer_packetEventList().stream()
                        .filter(pe -> !pe.isFired() && pe.getPacketEventType() == PacketEventType.FIRE_DELAYED && pe.getPacket() instanceof S2CMatchplayTriggerServe)
                        .findFirst();
                packetEvent.ifPresent(event -> packetEventHandler.remove(event, PacketEventHandler.ServerClient.SERVER));
            }

            if (setsDownGraded) {
                gameSession.setTimesCourtChanged(gameSession.getTimesCourtChanged() - 1);
                game.getPlayerLocationsOnMap().forEach(x -> x.setLocation(game.invertPointY(x)));
            }

            for (Client client : clients) {
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
                    if (rp.getPosition() == game.getPreviousServePlayerPosition()) {
                        serveType = ServeType.ServeBall;
                        game.setServePlayer(rp);
                    } else if (rp.getPosition() == game.getPreviousReceiverPlayerPosition()) {
                        serveType = ServeType.ReceiveBall;
                        game.setReceiverPlayer(rp);
                    }

                    ServeInfo playerServeInfo = new ServeInfo();
                    playerServeInfo.setPlayerPosition(rp.getPosition());
                    playerServeInfo.setPlayerStartLocation(game.getPlayerLocationsOnMap().get(rp.getPosition()));
                    playerServeInfo.setServeType(serveType);
                    serveInfos.add(playerServeInfo);

                    S2CMatchplayTeamWinsPoint matchplayTeamWinsPoint = new S2CMatchplayTeamWinsPoint((byte) 0, (byte) 0, (byte) game.getPointsRedTeam(), (byte) game.getPointsBlueTeam());
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsPoint, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);

                    if (setsDownGraded) {
                        S2CMatchplayTeamWinsSet matchplayTeamWinsSet = new S2CMatchplayTeamWinsSet((byte) game.getSetsRedTeam(), (byte) game.getSetsBlueTeam());
                        packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTeamWinsSet, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
                    }
                }
            }

            if (serveInfos.size() > 0) {
                if (!isSingles) {
                    game.setPlayerLocationsForDoubles(serveInfos);
                    ServeInfo receiver = serveInfos.stream()
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

                S2CMatchplayTriggerServe matchplayTriggerServe = new S2CMatchplayTriggerServe(serveInfos);
                for (Client client : clients)
                    packetEventHandler.push(packetEventHandler.createPacketEvent(client, matchplayTriggerServe, PacketEventType.DEFAULT, 0), PacketEventHandler.ServerClient.SERVER);
            }

            if (pointsBackSuccess) {
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Point back voted successfully.");
                GameManager.getInstance().getClientsInRoom(activeRoom.getRoomId()).forEach(c -> c.getConnection().getServer().sendToTcp(c.getConnection().getId(), chatRoomAnswerPacket));
            }
        }
    }
}
