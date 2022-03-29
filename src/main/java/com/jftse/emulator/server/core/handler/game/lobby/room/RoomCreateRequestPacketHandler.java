package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomCreateRequestPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomCreateRequestPacketHandler extends AbstractHandler {
    private C2SRoomCreateRequestPacket roomCreateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomCreateRequestPacket = new C2SRoomCreateRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if ((connection.getClient() != null && connection.getClient().getActiveRoom() != null) || connection.getClient().getActivePlayer() == null)
            return;

        Room room = new Room();
        room.setRoomId(GameManager.getInstance().getRoomId());
        room.setRoomName(roomCreateRequestPacket.getRoomName());
        room.setAllowBattlemon((byte) 0);

        room.setMode(roomCreateRequestPacket.getMode());
        room.setRule(roomCreateRequestPacket.getRule());
        room.setPlayers(roomCreateRequestPacket.getPlayers());
        room.setPrivate(roomCreateRequestPacket.isPrivate());
        room.setPassword(roomCreateRequestPacket.getPassword());
        room.setUnk1(roomCreateRequestPacket.getUnk1());
        room.setSkillFree(roomCreateRequestPacket.isSkillFree());
        room.setQuickSlot(roomCreateRequestPacket.isQuickSlot());
        room.setLevel(connection.getClient().getActivePlayer().getLevel());
        room.setLevelRange(roomCreateRequestPacket.getLevelRange());
        room.setBettingType(roomCreateRequestPacket.getBettingType());
        room.setBettingAmount(roomCreateRequestPacket.getBettingAmount());
        room.setBall(roomCreateRequestPacket.getBall());
        room.setMap((byte) 1);

        GameManager.getInstance().internalHandleRoomCreate(connection, room);
    }
}
