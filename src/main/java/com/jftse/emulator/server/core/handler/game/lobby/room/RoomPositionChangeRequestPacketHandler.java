package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomPositionChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPositionChangeAnswerPacket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RoomPositionChangeRequestPacketHandler extends AbstractHandler {
    private C2SRoomPositionChangeRequestPacket roomPositionChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomPositionChangeRequestPacket = new C2SRoomPositionChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        short positionToClaim = roomPositionChangeRequestPacket.getPosition();

        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            RoomPlayer requestingSlotChangePlayer = room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findAny()
                    .orElse(null);

            if (requestingSlotChangePlayer != null) {
                short requestingSlotChangePlayerOldPosition = requestingSlotChangePlayer.getPosition();
                if (requestingSlotChangePlayerOldPosition == positionToClaim) {
                    return;
                }

                boolean requestingSlotChangePlayerIsMaster = requestingSlotChangePlayer.isMaster();
                boolean slotIsInUse = false;
                int positionSize = room.getPositions().size();
                for (int i = 0; i < positionSize; i++) {
                    Short current = room.getPositions().poll();
                    room.getPositions().offer(current);

                    if (i == positionToClaim && current == RoomPositionState.InUse)
                        slotIsInUse = true;
                }

                if (slotIsInUse && !requestingSlotChangePlayerIsMaster) {
                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "You cannot claim this players slot");
                    connection.sendTCP(chatRoomAnswerPacket);
                    return;
                }

                boolean freeOldPosition = true;
                RoomPlayer playerInSlotToClaim = room.getRoomPlayerList().stream().filter(x -> x.getPosition() == positionToClaim).findAny().orElse(null);
                if (playerInSlotToClaim != null) {
                    freeOldPosition = false;
                    internalHandleRoomPositionChange(connection, room, playerInSlotToClaim, false,
                            playerInSlotToClaim.getPosition(), requestingSlotChangePlayerOldPosition);
                }

                internalHandleRoomPositionChange(connection, room, requestingSlotChangePlayer, freeOldPosition,
                        requestingSlotChangePlayerOldPosition, positionToClaim);
            }

            ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            roomPlayerList.forEach(rp -> {
                synchronized (rp) {
                    rp.setReady(false);
                }
            });
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(roomPlayerList));
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                }
            });
        }
    }

    private void internalHandleRoomPositionChange(final Connection connection, Room room, RoomPlayer roomPlayer, boolean freeOldPosition, short oldPosition, short newPosition) {
        int positionSize = room.getPositions().size();
        if (freeOldPosition) {
            for (int i = 0; i < positionSize; i++) {
                Short current = room.getPositions().poll();

                if (i == oldPosition && oldPosition == 9)
                    current = RoomPositionState.Locked;
                else if (i == oldPosition)
                    current = RoomPositionState.Free;

                room.getPositions().offer(current);
            }
        }
        positionSize = room.getPositions().size();
        for (int i = 0; i < positionSize; i++) {
            Short current = room.getPositions().poll();

            if (i == newPosition)
                current = RoomPositionState.InUse;

            room.getPositions().offer(current);
        }

        synchronized (roomPlayer) {
            roomPlayer.setPosition(newPosition);
        }

        S2CRoomPositionChangeAnswerPacket roomPositionChangePacket = new S2CRoomPositionChangeAnswerPacket((char) 0, oldPosition, newPosition);
        GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(roomPositionChangePacket);
            }
        });
    }
}
