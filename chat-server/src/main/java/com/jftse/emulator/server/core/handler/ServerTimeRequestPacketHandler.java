package com.jftse.emulator.server.core.handler;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.C2SServerTimeRequest)
public class ServerTimeRequestPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        Date currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

        Packet answer = new Packet(PacketOperations.S2CServerTimeAnswer);
        answer.write(currentTime);
        connection.sendTCP(answer);
    }
}
