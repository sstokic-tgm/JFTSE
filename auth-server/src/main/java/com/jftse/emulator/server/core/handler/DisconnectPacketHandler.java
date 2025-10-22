package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGDisconnectRequest;
import com.jftse.server.core.shared.packets.SMSGDisconnectResponse;

@PacketId(CMSGDisconnectRequest.PACKET_ID)
public class DisconnectPacketHandler implements PacketHandler<FTConnection, CMSGDisconnectRequest> {
    @Override
    public void handle(FTConnection connection, CMSGDisconnectRequest packet) throws Exception {
        SMSGDisconnectResponse response = SMSGDisconnectResponse.builder().status((byte) 0).build();
        connection.sendTCP(response);
    }
}
