package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.event.GameEventBus;
import com.jftse.emulator.server.core.life.event.GameEventType;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameDisplayPlayerStatsPacket;
import com.jftse.emulator.server.core.packets.matchplay.S2CGameSetNameColorAndRemoveBlackBar;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.matchplay.CMSGSkipped;
import com.jftse.server.core.shared.packets.matchplay.SMSGSkipped;
import com.jftse.server.core.thread.ThreadManager;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@PacketId(CMSGSkipped.PACKET_ID)
public class GameAnimationSkipTriggeredPacketHandler implements PacketHandler<FTConnection, CMSGSkipped> {
    @Override
    public void handle(FTConnection connection, CMSGSkipped packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getActiveRoom() == null
                || ftClient.getPlayer() == null || ftClient.getActiveGameSession() == null)
            return;

        Room room = ftClient.getActiveRoom();
        int roomStatus = room.getStatus();
        if (roomStatus != RoomStatus.AnimationSkipped) {
            return;
        }

        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;
        if (isTownSquare) {
            synchronized (room) {
                if (room.getStatus() != RoomStatus.NotRunning) {
                    room.setStatus(RoomStatus.NotRunning);
                }
            }
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

            SMSGSkipped skippedPacket = SMSGSkipped.builder().result((char) 0).build();
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(skippedPacket, ftClient.getConnection());

            GameEventBus.call(GameEventType.MP_GAME_ANIM_SKIP_TRIGGERED, ftClient, room, roomPlayer.get());

            S2CGameDisplayPlayerStatsPacket playerStatsPacket = new S2CGameDisplayPlayerStatsPacket(room);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(playerStatsPacket, ftClient.getConnection());

            ThreadManager.getInstance().schedule(() -> {
                FTClient client = connection.getClient();
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

                GameEventBus.call(GameEventType.MP_GAME_ANIM_SKIP_END, game, room);

                game.getHandleable().onStart(client);
            }, 8, TimeUnit.SECONDS);
        }
    }
}
