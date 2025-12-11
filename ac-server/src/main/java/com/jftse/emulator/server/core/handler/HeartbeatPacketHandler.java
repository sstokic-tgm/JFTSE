package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.ac.CMSGAntiCheatHeartBeat;
import com.jftse.server.core.shared.packets.ac.SMSGAntiCheatHeartBeat;

@PacketId(CMSGAntiCheatHeartBeat.PACKET_ID)
public class HeartbeatPacketHandler implements PacketHandler<FTConnection, CMSGAntiCheatHeartBeat> {
    @Override
    public void handle(FTConnection connection, CMSGAntiCheatHeartBeat packet) {
        SMSGAntiCheatHeartBeat response = SMSGAntiCheatHeartBeat.builder()
                .clientTimestamp(packet.getClientTimestamp())
                .serverTimestamp(System.currentTimeMillis())
                .build();
        connection.sendTCP(response);
    }
}
