package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.packets.lobby.room.S2CPetRequestRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.pet.CMSGRequestPet;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGRequestPet.PACKET_ID)
public class RoomRequestPetPacketHandler implements PacketHandler<FTConnection, CMSGRequestPet> {
    public RoomRequestPetPacketHandler() {
    }

    @Override
    public void handle(FTConnection connection, CMSGRequestPet packet) {
        final byte slot = packet.getSlot();

        try {
            FTClient ftClient = connection.getClient();
            if (ftClient == null) {
                return;
            }

            Room room = ftClient.getActiveRoom();
            if (room == null) {
                return;
            }

            if (room.getAllowBattlemon() == 0) {
                S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.PET_NOT_ALLOWED, false, slot, null);
                connection.sendTCP(petRequestRoomAnswerPacket);
                return;
            }

            RoomPlayer roomPlayer = ftClient.getRoomPlayer();
            if (roomPlayer == null) {
                return;
            }

            boolean isAdd = false;
            if (ftClient.getActivePet() == null) {
                S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.NO_PET_SELECTED, false, slot, null);
                connection.sendTCP(petRequestRoomAnswerPacket);
                return;
            }

            final boolean slotNotFree = room.getRoomPlayerList().stream().anyMatch(rp -> rp.getPosition() == (slot + 2));
            if (slotNotFree) {
                S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.NO_FREE_SLOT, false, slot, null);
                connection.sendTCP(petRequestRoomAnswerPacket);
                return;
            }

            Pet pet = roomPlayer.getPet();
            if (pet != null) {
                roomPlayer.setPetId(null);
            } else {
                roomPlayer.setPetId(ftClient.getActivePet().getId());
                pet = roomPlayer.getPet();
                isAdd = true;
            }

            S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.SUCCESS, isAdd, slot, pet);
            connection.sendTCP(petRequestRoomAnswerPacket);
        } catch (Exception e) {
            S2CPetRequestRoomAnswerPacket petRequestRoomAnswerPacket = new S2CPetRequestRoomAnswerPacket(S2CPetRequestRoomAnswerPacket.CAN_NOT_ADD_PET, false, slot, null);
            connection.sendTCP(petRequestRoomAnswerPacket);

            log.error("Error in RoomRequestPetPacketHandler", e);
        }
    }
}
