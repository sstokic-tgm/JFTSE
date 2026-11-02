package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerJoinSession;
import com.jftse.server.core.shared.packets.matchplay.SMSGMatchRelayRegistrationResult;
import lombok.extern.log4j.Log4j2;

@PacketId(CMSGPlayerJoinSession.PACKET_ID)
@Log4j2
public class RegisterPlayerForSessionHandler implements PacketHandler<FTConnection, CMSGPlayerJoinSession> {
    @Override
    public void handle(FTConnection connection, CMSGPlayerJoinSession matchplayPlayerIdsInSessionPacket) {
        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        boolean isSpectator = matchplayPlayerIdsInSessionPacket.getIsSpectator();
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        SMSGMatchRelayRegistrationResult.Builder sessionResultBuilder = new SMSGMatchRelayRegistrationResult.Builder();

        if (playerId > 0) {
            if (isSpectator) {
                connection.getChannelHandlerContext().pipeline().remove("readTimeoutHandler");
            }

            FTClient client = connection.getClient();
            client.setGameSessionId(sessionId);
            client.setPlayerId(playerId);
            client.setSpectator(isSpectator);

            log.info("playerId {} connected for session: {}", playerId, sessionId);

            RelayManager.getInstance().addClientToSession(sessionId, client);

            sessionResultBuilder.status((byte) 0);
        } else {
            sessionResultBuilder.status((byte) 1);
        }

        connection.sendTCP(sessionResultBuilder.build());

        if (playerId <= 0) {
            log.error("Invalid relay player ID: {}", playerId);
            connection.close();
        }
    }
}
