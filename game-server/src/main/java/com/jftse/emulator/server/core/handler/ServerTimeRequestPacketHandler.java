package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGServerTime;
import com.jftse.server.core.shared.packets.tutorial.SMSGServerTime;
import com.jftse.server.core.util.GameTime;

import java.time.ZoneId;
import java.util.Date;

@PacketId(CMSGServerTime.PACKET_ID)
public class ServerTimeRequestPacketHandler implements PacketHandler<FTConnection, CMSGServerTime> {
    @Override
    public void handle(FTConnection connection, CMSGServerTime packet) {
        Date currentTime = Date.from(GameTime.getDateAndTime().atZone(ZoneId.systemDefault()).toInstant());
        SMSGServerTime time = SMSGServerTime.builder().currentTime(currentTime).build();
        connection.sendTCP(time);
    }
}
