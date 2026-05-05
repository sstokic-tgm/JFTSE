package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.constants.BallHitAction;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGBallAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGBallAnimation;
import com.jftse.server.core.shared.packets.translator.BallAnimationTranslator;
import com.jftse.server.core.shared.rabbit.messages.MatchBallSyncMessage;

import java.util.List;

@PacketId(CMSGBallAnimation.PACKET_ID)
public class BallAnimationHandler implements PacketHandler<FTConnection, CMSGBallAnimation> {
    private static final IPacketTranslator<SMSGBallAnimation, CMSGBallAnimation> translator = new BallAnimationTranslator();

    @Override
    public void handle(FTConnection connection, CMSGBallAnimation packet) {
        FTClient client = connection.getClient();

        MatchBallSyncMessage mbsm = MatchBallSyncMessage.builder()
                .gameSessionId(client.getGameSessionId().orElse(null))
                .playerId(client.getPlayerId())
                .playerPos((int) packet.getPlayerPosition())
                .hitAct(BallHitAction.valueOf(packet.getHitAct()))
                .powerLevel((int) packet.getPowerLevel())
                .speed(packet.getSpeed() / 100.0f)
                .curveControl(packet.getCurveControl() / 100.0f)
                .shotCode((int) packet.getShotCode())
                .specialShotId((int) packet.getSpecialShotId())
                .build();
        RProducerService.getInstance().send(mbsm, "game.stats.match.rally", "MatchplaySystem(RelayServer)");

        SMSGBallAnimation relayPacket = packet.translate(translator);
        client.getGameSessionId().ifPresent(gameSessionId -> {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(gameSessionId);
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        });
    }
}
