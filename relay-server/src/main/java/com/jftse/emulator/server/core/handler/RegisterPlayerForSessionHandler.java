package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerJoinSession;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerJoinSessionResult;
import lombok.extern.log4j.Log4j2;

@PacketId(CMSGPlayerJoinSession.PACKET_ID)
@Log4j2
public class RegisterPlayerForSessionHandler implements PacketHandler<FTConnection, CMSGPlayerJoinSession> {
    @Override
    public void handle(FTConnection connection, CMSGPlayerJoinSession matchplayPlayerIdsInSessionPacket) {
        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        SMSGPlayerJoinSessionResult.Builder sessionResultBuilder = new SMSGPlayerJoinSessionResult.Builder();

        if (playerId != -1) {
            FTClient client = connection.getClient();
            client.setGameSessionId(sessionId);

            log.info("playerId " + playerId + " connected for session: " + sessionId);

            RelayManager.getInstance().addClientToSession(sessionId, client);

            sessionResultBuilder.result((byte) 0);
        } else {
            sessionResultBuilder.result((byte) 1);
        }

        connection.sendTCP(sessionResultBuilder.build());

        if (playerId == -1) {
            log.error("playerId is -1");
            connection.close();
        }
    }
}
