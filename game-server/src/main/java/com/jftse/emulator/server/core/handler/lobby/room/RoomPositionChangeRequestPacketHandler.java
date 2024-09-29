package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomPositionChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPositionChangeAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPositionSwapPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomReadyChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

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
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        if (!ftClient.getIsChangingSlot().compareAndSet(false, true)) {
            return;
        }

        short positionToClaim = roomPositionChangeRequestPacket.getPosition();

        Room room = ftClient.getActiveRoom();
        RoomPlayer requestingSlotChangePlayer = ftClient.getRoomPlayer();
        if (room != null) {
            if (requestingSlotChangePlayer != null) {
                short requestingSlotChangePlayerOldPosition = requestingSlotChangePlayer.getPosition();
                if (requestingSlotChangePlayerOldPosition == positionToClaim) {
                    ftClient.getIsChangingSlot().set(false);
                    return;
                }

                boolean requestingSlotChangePlayerIsMaster = requestingSlotChangePlayer.isMaster();
                boolean slotIsInUse = room.getPositions().get(positionToClaim) == RoomPositionState.InUse;
                if (slotIsInUse && !requestingSlotChangePlayerIsMaster) {
                    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "You cannot claim this players slot");
                    connection.sendTCP(chatRoomAnswerPacket);
                    ftClient.getIsChangingSlot().set(false);
                    return;
                }

                RoomPlayer playerInSlotToClaim = room.getRoomPlayerList().stream().filter(x -> x.getPosition() == positionToClaim).findAny().orElse(null);

                if (playerInSlotToClaim != null) {
                    if (requestingSlotChangePlayerOldPosition == 9) {
                        room.getPositions().set(requestingSlotChangePlayerOldPosition, RoomPositionState.Locked);
                    }

                    playerInSlotToClaim.setPosition(requestingSlotChangePlayerOldPosition);
                    requestingSlotChangePlayer.setPosition(positionToClaim);

                    S2CRoomPositionSwapPacket roomPositionSwapPacket = new S2CRoomPositionSwapPacket(requestingSlotChangePlayerOldPosition, positionToClaim);
                    GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomPositionSwapPacket, ftClient.getConnection());

                    for (RoomPlayer roomPlayer : room.getRoomPlayerList()) {
                        synchronized (roomPlayer) {
                            roomPlayer.setReady(false);
                        }

                        S2CRoomReadyChangeAnswerPacket roomReadyChangeAnswerPacket = new S2CRoomReadyChangeAnswerPacket(roomPlayer.getPosition(), roomPlayer.isReady());
                        GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomReadyChangeAnswerPacket, ftClient.getConnection());
                    }

                } else {
                    if (requestingSlotChangePlayerOldPosition == 9) {
                        room.getPositions().set(requestingSlotChangePlayerOldPosition, RoomPositionState.Locked);
                    } else {
                        room.getPositions().set(requestingSlotChangePlayerOldPosition, RoomPositionState.Free);
                    }

                    room.getPositions().set(positionToClaim, RoomPositionState.InUse);

                    synchronized (requestingSlotChangePlayer) {
                        requestingSlotChangePlayer.setPosition(positionToClaim);
                    }

                    S2CRoomPositionChangeAnswerPacket roomPositionChangePacket = new S2CRoomPositionChangeAnswerPacket((char) 0, requestingSlotChangePlayerOldPosition, positionToClaim);
                    GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomPositionChangePacket, ftClient.getConnection());
                }
            }
        }
        ftClient.getIsChangingSlot().set(false);
    }
}
