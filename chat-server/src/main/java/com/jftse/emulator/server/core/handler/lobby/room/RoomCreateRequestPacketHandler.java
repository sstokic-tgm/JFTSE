package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomCreate;

@PacketId(CMSGRoomCreate.PACKET_ID)
public class RoomCreateRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomCreate> {
    @Override
    public void handle(FTConnection connection, CMSGRoomCreate packet) {
        FTClient client = connection.getClient();
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if ((client != null && client.getActiveRoom() != null) || client == null || client.getPlayer() == null)
            return;

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        Room room = new Room();
        room.setRoomId(GameManager.getInstance().getRoomId());
        room.setRoomName(packet.getRoomName());
        room.setRoomType(packet.getRoomType());
        room.setAllowBattlemon(room.getRoomType() == 2 ? (byte) 1 : (byte) 0);

        room.setMode(packet.getMode());
        room.setRule(packet.getRule());
        room.setPlayers(packet.getPlayers());
        room.setPrivate(packet.getIsPrivate());
        room.setPassword(packet.getPassword());
        room.setSkillFree(packet.getSkillFree());
        room.setQuickSlot(packet.getQuickSlot());
        room.setLevel(client.getPlayer().getLevel());
        room.setLevelRange(packet.getLevelRange());
        room.setBettingType(packet.getBettingType());
        room.setBettingAmount(packet.getBettingAmount());
        room.setBall(packet.getBall());
        room.setMap(packet.getMapId());

        GameManager.getInstance().internalHandleRoomCreate(client.getConnection(), room);

        client.getIsJoiningOrLeavingRoom().set(false);
    }
}
