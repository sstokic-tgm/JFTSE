package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerAnimation;
import com.jftse.server.core.shared.packets.translator.PlayerAnimationTranslator;

import java.util.List;

@PacketId(CMSGPlayerAnimation.PACKET_ID)
public class PlayerAnimationHandler implements PacketHandler<FTConnection, CMSGPlayerAnimation> {
    private static final IPacketTranslator<SMSGPlayerAnimation, CMSGPlayerAnimation> translator = new PlayerAnimationTranslator();

    @Override
    public void handle(FTConnection connection, CMSGPlayerAnimation packet) {
        connection.getClient()
                .getAnimationDebugStats()
                .recordPlayerAnimation(packet.getAnimationType());

        SMSGPlayerAnimation relayPacket = packet.translate(translator);
        connection.getClient().getGameSessionId().ifPresent(gameSessionId -> {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(gameSessionId);
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        });
    }
}
