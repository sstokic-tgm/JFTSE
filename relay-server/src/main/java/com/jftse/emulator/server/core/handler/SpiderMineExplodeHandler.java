package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGSpiderMineExplode;
import com.jftse.server.core.shared.packets.relay.SMSGSpiderMineExplode;
import com.jftse.server.core.shared.packets.translator.SpiderMineExplodeTranslator;

import java.util.List;

@PacketId(CMSGSpiderMineExplode.PACKET_ID)
public class SpiderMineExplodeHandler implements PacketHandler<FTConnection, CMSGSpiderMineExplode> {
    private static final IPacketTranslator<SMSGSpiderMineExplode, CMSGSpiderMineExplode> translator = new SpiderMineExplodeTranslator();

    @Override
    public void handle(FTConnection connection, CMSGSpiderMineExplode packet) {
        SMSGSpiderMineExplode relayPacket = packet.translate(translator);
        connection.getClient().getGameSessionId().ifPresent(gameSessionId -> {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(gameSessionId);
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        });
    }
}
