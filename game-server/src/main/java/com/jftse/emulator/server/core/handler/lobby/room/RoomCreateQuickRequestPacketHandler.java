package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomType;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomCreateQuick;

import java.util.Random;

@PacketId(CMSGRoomCreateQuick.PACKET_ID)
public class RoomCreateQuickRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomCreateQuick> {
    @Override
    public void handle(FTConnection connection, CMSGRoomCreateQuick packet) {
        FTClient client = connection.getClient();
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if (client != null && client.getActiveRoom() != null)
            return;

        if (client == null)
            return;

        if (packet.getRoomType() == RoomType.BATTLEMON) {
            //GameManager.getInstance().handleChatLobbyJoin(client);
            return;
        }

        Player player = client.getPlayer();
        if (player == null)
            return;

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        byte playerSize = packet.getPlayers();

        Room room = new Room();
        room.setRoomId(GameManager.getInstance().getRoomId());
        room.setRoomName(String.format("%s's room", player.getName()));
        room.setRoomType(packet.getRoomType());
        room.setAllowBattlemon(room.getRoomType() == 2 ? (byte) 1 : (byte) 0);

        if (packet.getMode() == -1) {
            final Random random = new Random();
            packet.setMode((byte) random.nextInt(2));
        }

        room.setMode(packet.getMode());
        room.setRule((byte) 0);

        if (packet.getMode() == GameMode.GUARDIAN)
            room.setPlayers((byte) 4);
        else
            room.setPlayers(playerSize == 0 ? 2 : playerSize);

        if (room.getRoomType() == RoomType.BATTLEMON)
            room.setPlayers((byte) 4);

        room.setPrivate(false);
        room.setSkillFree(false);
        room.setQuickSlot(false);
        room.setLevel(player.getLevel());
        room.setLevelRange((byte) -1);
        room.setBettingType('0');
        room.setBettingAmount(0);
        room.setBall(1);
        room.setMap((byte) 0);

        GameManager.getInstance().internalHandleRoomCreate(client.getConnection(), room);

        client.getIsJoiningOrLeavingRoom().set(false);
    }
}
