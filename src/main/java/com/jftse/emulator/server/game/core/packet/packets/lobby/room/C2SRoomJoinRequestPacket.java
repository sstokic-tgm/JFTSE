package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class C2SRoomJoinRequestPacket extends Packet {
    private short roomId;
    private String password;

    public C2SRoomJoinRequestPacket(Packet packet, List<Room> roomList) {
        super(packet);

        this.roomId = this.readShort();
        this.readByte();

        Room room = roomList.stream()
                .filter(r -> r.getRoomId() == this.roomId)
                .findAny()
                .orElse(null);

        if (room != null && room.isPrivate() && !StringUtils.isEmpty(room.getPassword()))
            this.password = this.readUnicodeString();
    }
}