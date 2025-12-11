package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.SMSGChatMessageRoom;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangePosition;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangePosition;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeReady;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomSwapPosition;

@PacketId(CMSGRoomChangePosition.PACKET_ID)
public class RoomPositionChangeRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomChangePosition> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangePosition packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        if (!ftClient.getIsChangingSlot().compareAndSet(false, true)) {
            return;
        }

        short positionToClaim = packet.getPosition();

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
                    SMSGChatMessageRoom msg = SMSGChatMessageRoom.builder()
                            .type((byte) 2)
                            .sender("Room")
                            .message("You cannot claim this players slot")
                            .build();
                    connection.sendTCP(msg);
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

                    SMSGRoomSwapPosition swapPosition = SMSGRoomSwapPosition.builder()
                            .oldPosition(requestingSlotChangePlayerOldPosition)
                            .newPosition(positionToClaim)
                            .build();
                    GameManager.getInstance().sendPacketToAllClientsInSameRoom(swapPosition, ftClient.getConnection());

                    for (RoomPlayer roomPlayer : room.getRoomPlayerList()) {
                        synchronized (roomPlayer) {
                            roomPlayer.setReady(false);
                        }

                        SMSGRoomChangeReady changeReady = SMSGRoomChangeReady.builder()
                                .position(roomPlayer.getPosition())
                                .ready(roomPlayer.isReady())
                                .build();
                        GameManager.getInstance().sendPacketToAllClientsInSameRoom(changeReady, ftClient.getConnection());
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

                    SMSGRoomChangePosition roomChangePosition = SMSGRoomChangePosition.builder()
                            .result((char) 0)
                            .oldPosition(requestingSlotChangePlayerOldPosition)
                            .newPosition(positionToClaim)
                            .build();
                    GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomChangePosition, ftClient.getConnection());
                }
            }
        }
        ftClient.getIsChangingSlot().set(false);
    }
}
