package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomCreateQuickRequestPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomCreateQuickRequestPacketHandler extends AbstractHandler {
    private C2SRoomCreateQuickRequestPacket roomQuickCreateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomQuickCreateRequestPacket = new C2SRoomCreateQuickRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if (connection.getClient() != null && connection.getClient().getActiveRoom() != null)
            return;

        if (roomQuickCreateRequestPacket.getMode() == GameMode.BATTLEMON)
            return;

        Player player = connection.getClient().getPlayer();
        if (player == null)
            return;

        byte playerSize = roomQuickCreateRequestPacket.getPlayers();

        Room room = new Room();
        room.setRoomId(GameManager.getInstance().getRoomId());
        room.setRoomName(String.format("%s's room", player.getName()));
        room.setAllowBattlemon(roomQuickCreateRequestPacket.getAllowBattlemon());

        room.setMode(roomQuickCreateRequestPacket.getMode());
        room.setRule((byte) 0);

        if (roomQuickCreateRequestPacket.getMode() == GameMode.GUARDIAN)
            room.setPlayers((byte) 4);
        else
            room.setPlayers(playerSize == 0 ? 2 : playerSize);

        room.setPrivate(false);
        room.setUnk1((byte) 0);
        room.setSkillFree(false);
        room.setQuickSlot(false);
        room.setLevel(player.getLevel());
        room.setLevelRange((byte) -1);
        room.setBettingType('0');
        room.setBettingAmount(0);
        room.setBall(1);
        room.setMap((byte) 1);

        GameManager.getInstance().internalHandleRoomCreate(connection, room);
    }
}
