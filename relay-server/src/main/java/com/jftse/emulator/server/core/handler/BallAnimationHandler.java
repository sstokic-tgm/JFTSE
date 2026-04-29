package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGBallAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGBallAnimation;
import com.jftse.server.core.shared.packets.translator.BallAnimationTranslator;

import java.util.List;

@PacketId(CMSGBallAnimation.PACKET_ID)
public class BallAnimationHandler implements PacketHandler<FTConnection, CMSGBallAnimation> {
    private static final IPacketTranslator<SMSGBallAnimation, CMSGBallAnimation> translator = new BallAnimationTranslator();

    @Override
    public void handle(FTConnection connection, CMSGBallAnimation packet) {
        connection.getClient().getAnimationDebugStats().recordBallAnimation(
                packet.getHitAct(),
                packet.getPowerLevel(),
                packet.getSpeed()
        );

        SMSGBallAnimation relayPacket = packet.translate(translator);
        connection.getClient().getGameSessionId().ifPresent(gameSessionId -> {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(gameSessionId);
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        });
    }
}
