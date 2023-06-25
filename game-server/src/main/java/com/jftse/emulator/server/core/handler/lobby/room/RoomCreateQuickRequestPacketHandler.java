package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomCreateQuickRequestPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomCreateQuick)
public class RoomCreateQuickRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomCreateQuickRequestPacket roomQuickCreateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomQuickCreateRequestPacket = new C2SRoomCreateQuickRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        // prevent multiple room creations, this might have to be adjusted into a "room join answer"
        if (client != null && client.getActiveRoom() != null)
            return;

        if (client == null)
            return;

        if (roomQuickCreateRequestPacket.getMode() == GameMode.BATTLEMON)
            return;

        Player player = client.getPlayer();
        if (player == null)
            return;

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

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

        GameManager.getInstance().internalHandleRoomCreate(client.getConnection(), room);

        client.getIsJoiningOrLeavingRoom().set(false);
    }
}
