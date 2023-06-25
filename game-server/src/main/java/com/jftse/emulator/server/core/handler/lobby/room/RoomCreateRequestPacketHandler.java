package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomCreateRequestPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomCreate)
public class RoomCreateRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomCreateRequestPacket roomCreateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomCreateRequestPacket = new C2SRoomCreateRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if ((client != null && client.getActiveRoom() != null) || client == null || client.getPlayer() == null)
            return;

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

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
        room.setLevel(client.getPlayer().getLevel());
        room.setLevelRange(roomCreateRequestPacket.getLevelRange());
        room.setBettingType(roomCreateRequestPacket.getBettingType());
        room.setBettingAmount(roomCreateRequestPacket.getBettingAmount());
        room.setBall(roomCreateRequestPacket.getBall());
        room.setMap((byte) 1);

        GameManager.getInstance().internalHandleRoomCreate(client.getConnection(), room);

        client.getIsJoiningOrLeavingRoom().set(false);
    }
}
