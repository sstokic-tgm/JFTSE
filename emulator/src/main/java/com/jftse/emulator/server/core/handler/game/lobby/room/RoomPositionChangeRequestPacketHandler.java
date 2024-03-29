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
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        short positionToClaim = roomPositionChangeRequestPacket.getPosition();

        Room room = connection.getClient().getActiveRoom();
        RoomPlayer requestingSlotChangePlayer = connection.getClient().getRoomPlayer();
        if (room != null) {
            if (requestingSlotChangePlayer != null) {
                short requestingSlotChangePlayerOldPosition = requestingSlotChangePlayer.getPosition();
                if (requestingSlotChangePlayerOldPosition == positionToClaim) {
                    return;
                }

                boolean requestingSlotChangePlayerIsMaster = requestingSlotChangePlayer.isMaster();
                boolean slotIsInUse = room.getPositions().get(positionToClaim) == RoomPositionState.InUse;
                if (slotIsInUse && !requestingSlotChangePlayerIsMaster) {
                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "You cannot claim this players slot");
                    connection.sendTCP(chatRoomAnswerPacket);
                    return;
                }

                boolean freeOldPosition = true;
                RoomPlayer playerInSlotToClaim = room.getRoomPlayerList().stream().filter(x -> x.getPosition() == positionToClaim).findAny().orElse(null);
                if (playerInSlotToClaim != null) {
                    freeOldPosition = false;
                    internalHandleRoomPositionChange(room, playerInSlotToClaim, false,
                            playerInSlotToClaim.getPosition(), requestingSlotChangePlayerOldPosition);
                }

                internalHandleRoomPositionChange(room, requestingSlotChangePlayer, freeOldPosition,
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

    private void internalHandleRoomPositionChange(Room room, RoomPlayer roomPlayer, boolean freeOldPosition, short oldPosition, short newPosition) {
        if (freeOldPosition) {
            if (oldPosition == 9) {
                room.getPositions().set(oldPosition, RoomPositionState.Locked);
            } else {
                room.getPositions().set(oldPosition, RoomPositionState.Free);
            }
        }

        room.getPositions().set(newPosition, RoomPositionState.InUse);

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
