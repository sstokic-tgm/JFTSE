package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomCreateQuickRequestPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.HomeService;

@PacketOperationIdentifier(PacketOperations.C2SRoomCreateQuick)
public class RoomCreateQuickRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomCreateQuickRequestPacket roomQuickCreateRequestPacket;

    private final HomeService homeService;

    public RoomCreateQuickRequestPacketHandler() {
        this.homeService = ServiceManager.getInstance().getHomeService();
    }

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

        Player player = client.getPlayer();
        if (player == null)
            return;

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        byte players = roomQuickCreateRequestPacket.getPlayers();
        if (players == 0) {
            players = 8;
        }

        Room room = new Room();
        room.setRoomId(GameManager.getInstance().getRoomId());
        room.setRoomName(String.format("%s's room", player.getName()));
        room.setRoomType(roomQuickCreateRequestPacket.getRoomType());
        room.setAllowBattlemon(room.getRoomType() == 2 ? (byte) 1 : (byte) 0);

        room.setMode(roomQuickCreateRequestPacket.getMode());
        room.setRule((byte) 0);
        room.setPlayers(players);
        room.setPrivate(false);
        room.setSkillFree(false);
        room.setQuickSlot(false);
        room.setLevel(player.getLevel());
        room.setLevelRange((byte) -1);
        room.setBettingType('0');
        room.setBettingAmount(0);
        room.setBall(1);

        if (room.getMode() == 1) {
            AccountHome accountHome = homeService.findAccountHomeByAccountId(player.getAccount().getId());
            if (accountHome != null) {
                room.setMap(accountHome.getLevel());
            } else {
                room.setMap((byte) 0);
            }
        } else {
            room.setMap((byte) 0);
        }

        GameManager.getInstance().internalHandleRoomCreate(client.getConnection(), room);

        client.getIsJoiningOrLeavingRoom().set(false);
    }
}
