package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ServerTimeRequestPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        Date currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

        Packet answer = new Packet(PacketOperations.S2CServerTimeAnswer.getValueAsChar());
        answer.write(currentTime);
        connection.sendTCP(answer);
    }
}
