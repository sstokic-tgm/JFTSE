package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CRoomListAnswerPacket extends Packet {
    public S2CRoomListAnswerPacket(List<Room> roomList) {
        super(PacketOperations.S2CRoomListAnswer);

        this.write((char) roomList.size());
        for (Room room : roomList) {
            int nonSpectatorPlayerCount = (int) room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() < 4)
                    .count();
            byte maxPositionsAvailable = (byte) room.getPositions().stream()
                    .limit(4)
                    .filter(x -> x != RoomPositionState.Locked)
                    .count();
            this.write(room.getRoomId());
            this.write(room.getRoomName());
            this.write(room.getAllowBattlemon());
            this.write(room.getMode());
            this.write(room.getRule());
            this.write((byte) 0); // betting mode
            this.write((byte) 0); // betting coins
            this.write(room.getBettingAmount());
            this.write(room.getBall());
            this.write(maxPositionsAvailable);
            this.write(room.isPrivate());
            this.write(room.getLevel());
            this.write(room.getLevelRange());
            this.write((byte) 0); // allow battlemon
            this.write(room.getMap());
            this.write(room.isSkillFree());
            this.write(room.isQuickSlot());
            this.write(BitKit.fromUnsignedInt(nonSpectatorPlayerCount));
            this.write((byte) 0); // ?
            this.write((byte) 0); // ?
        }
    }
}