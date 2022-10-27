package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomPositionChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPositionChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketOperationIdentifier(PacketOperations.C2SRoomPositionChange)
public class RoomPositionChangeRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomPositionChangeRequestPacket roomPositionChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomPositionChangeRequestPacket = new C2SRoomPositionChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        short positionToClaim = roomPositionChangeRequestPacket.getPosition();

        Room room = ftClient.getActiveRoom();
        RoomPlayer requestingSlotChangePlayer = ftClient.getRoomPlayer();
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
                if (c.getConnection() != null) {
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
            if (c.getConnection() != null) {
                c.getConnection().sendTCP(roomPositionChangePacket);
            }
        });
    }
}
