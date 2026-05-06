package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGServerTime;
import com.jftse.server.core.shared.packets.SMSGServerTime;
import com.jftse.server.core.util.GameTime;
import com.jftse.server.core.util.Time;

@PacketId(CMSGServerTime.PACKET_ID)
public class ServerTimeRequestPacketHandler implements PacketHandler<FTConnection, CMSGServerTime> {
    @Override
    public void handle(FTConnection connection, CMSGServerTime packet) {
        SMSGServerTime time = SMSGServerTime.builder().currentTime(Time.toFileTimeUTC(GameTime.now().toEpochMilli())).build();
        connection.sendTCP(time);
    }
}
