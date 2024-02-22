package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.packets.lobby.room.C2SPetRequestRoomPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CPetRequestRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SPetRequestRoom)
public class RoomRequestPetPacketHandler extends AbstractPacketHandler {
    private C2SPetRequestRoomPacket petRequestRoomPacket;

    public RoomRequestPetPacketHandler() {
    }

    @Override
    public boolean process(Packet packet) {
        petRequestRoomPacket = new C2SPetRequestRoomPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        final byte slot = petRequestRoomPacket.getSlot();

        try {
            FTClient ftClient = (FTClient) connection.getClient();
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
