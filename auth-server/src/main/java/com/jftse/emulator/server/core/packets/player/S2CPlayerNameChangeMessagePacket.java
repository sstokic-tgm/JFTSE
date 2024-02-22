package com.jftse.emulator.server.core.packets.player;

import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class S2CPlayerNameChangeMessagePacket extends Packet {
    public static final byte RESULT_SUCCESS = 0;
    public static final byte MSG_NAME_CHANGE_NEXT = -4;
    public static final byte MSG_NAME_CHANGE_UNABLE = -3;
    public static final byte MSG_ALREADY_USE_NICKNAME = -2;
    public static final byte MSG_CHARACTER_NOT_FOUND = -1;

    public S2CPlayerNameChangeMessagePacket(byte result, Player player) {
        super(PacketOperations.S2CPlayerNameChangeMessage);

        this.write(result);
        if (result == RESULT_SUCCESS) {
            this.write(Math.toIntExact(player.getId()));
            this.write(player.getName());
        } else if (result == MSG_NAME_CHANGE_NEXT) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(player.getLastNameChangeDate());
            calendar.add(Calendar.DATE, 30);
            Date date = calendar.getTime();
            this.write(date);
        }
    }
}
