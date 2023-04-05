package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameDisplayPlayerStatsPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.thread.ThreadManager;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@PacketOperationIdentifier(PacketOperations.C2SGameAnimationSkipTriggered)
public class GameAnimationSkipTriggeredPacketHandler extends AbstractPacketHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getActiveRoom() == null
                || ftClient.getPlayer() == null || ftClient.getActiveGameSession() == null)
            return;

        Room room = ftClient.getActiveRoom();
        int roomStatus = room.getStatus();
        if (roomStatus != RoomStatus.AnimationSkipped) {
            return;
        }

        Player player = ftClient.getPlayer();

        Optional<RoomPlayer> roomPlayer = room.getRoomPlayerList().stream()
                .filter(x -> x.getPlayerId().equals(player.getId()))
                .findFirst();

        if (roomPlayer.isPresent()) {
            synchronized (room) {
                if (room.getStatus() == roomStatus) {
                    room.setStatus(RoomStatus.Running);
                }
            }

            Packet gameAnimationSkipPacket = new Packet(PacketOperations.S2CGameAnimationSkip);
            gameAnimationSkipPacket.write((char) 0);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(gameAnimationSkipPacket, ftClient.getConnection());

            S2CGameDisplayPlayerStatsPacket playerStatsPacket = new S2CGameDisplayPlayerStatsPacket(room);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(playerStatsPacket, ftClient.getConnection());

            ThreadManager.getInstance().schedule(() -> {
                FTClient client = (FTClient) connection.getClient();
                if (client == null) return;

                Room threadRoom = client.getActiveRoom();
                if (threadRoom == null || threadRoom.getStatus() != RoomStatus.Running) {
                    return;
                }

                S2CGameSetNameColorAndRemoveBlackBar setNameColorAndRemoveBlackBarPacket = new S2CGameSetNameColorAndRemoveBlackBar(room);
                GameManager.getInstance().sendPacketToAllClientsInSameGameSession(setNameColorAndRemoveBlackBarPacket, client.getConnection());

                GameSession gameSession = client.getActiveGameSession();
                MatchplayGame game = gameSession.getMatchplayGame();
                if (game == null)
                    return;

                game.getHandleable().onStart(client);
            }, 8, TimeUnit.SECONDS);
        }
    }
}
