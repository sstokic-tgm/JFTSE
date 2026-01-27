package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGUnknown0x1071;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGUnknown0x1071.PACKET_ID)
public class Unknown0x1071PacketHandler implements PacketHandler<FTConnection, CMSGUnknown0x1071> {
    @Override
    public void handle(FTConnection connection, CMSGUnknown0x1071 packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        int oldSceneId = client.getSceneId();
        int newSceneId = packet.getSceneId();
        client.setSceneId(newSceneId);
        log.debug("{} changed scene from {} to {}", client.getPlayer().getName(), oldSceneId, newSceneId);
    }
}
