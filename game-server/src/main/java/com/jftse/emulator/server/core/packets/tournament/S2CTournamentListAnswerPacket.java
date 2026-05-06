package com.jftse.emulator.server.core.packets.tournament;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Calendar;
import java.util.TimeZone;

public class S2CTournamentListAnswerPacket extends Packet {
    public S2CTournamentListAnswerPacket() {
        super(PacketOperations.S2CTournamentListAnswer);

        this.write((byte) 1); // list size

        this.write(1);  // id maybe
        this.write((byte) 1); // 0 Entry Type: Guild Match, 1 Entry Type: Individual Match
        this.write((byte) 0); // 0 basic mode, 1 battle mode
        this.write("Match Basic"); // tournament title

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        this.write(calendar.getTime()); // application period start
        this.write(calendar.getTime()); // application period end
        this.write(calendar.getTime()); // total period start
        this.write(calendar.getTime()); // total period end

        this.write(2); // ?
        this.write(3); // ?
        this.write((byte) 0); // ?
        this.write(4); // ?

        this.write(calendar.getTime()); // ?

        this.write((byte) 0); // tournament status: 0 Ready, 1 Applying, 2 Preparing for qualifying matches, 3 Qualifying matches, 4 Preparing for final matches, 5 Final matches, 6 Complete, 7 Suspended, 8 Cancelled, > 9 ERROR
        this.write((byte) 0); // ?

        this.write(1); // ?
        this.write(1); // ?

        // winning items
        for (int i = 0; i < 5; i++) {
            this.write(287); // productIndex
            this.write(0); // use0-2
        }

        // ?? maybe players
        for (int i = 0; i < 16; i++) {
            this.write(1);
            this.write(1);
        }

        // ?? maybe players
        for (int i = 0; i < 16; i++) {
            this.write(1);
            this.write(1);
        }
    }
}
