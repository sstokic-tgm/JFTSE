package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.packets.C2SMatchplayPlayerIdsInSessionPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.extern.log4j.Log4j2;

@PacketOperationIdentifier(PacketOperations.C2SMatchplayRegisterPlayerForRelay)
@Log4j2
public class RegisterPlayerForSessionHandler extends AbstractPacketHandler {
    private C2SMatchplayPlayerIdsInSessionPacket matchplayPlayerIdsInSessionPacket;

    @Override
    public boolean process(Packet packet) {
        matchplayPlayerIdsInSessionPacket = new C2SMatchplayPlayerIdsInSessionPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int playerId = matchplayPlayerIdsInSessionPacket.getPlayerIds().stream().findFirst().orElse(-1);
        int sessionId = matchplayPlayerIdsInSessionPacket.getSessionId();

        if (playerId != -1) {
            FTClient client = connection.getClient();
            client.setGameSessionId(sessionId);

            log.info("playerId " + playerId + " connected for session: " + sessionId);

            RelayManager.getInstance().addClient(sessionId, client);

            Packet answer = new Packet(PacketOperations.S2CMatchplayAckRelayConnection.getValue());
            answer.write((byte) 0);
            connection.sendTCP(answer);
        } else {
            log.error("playerId is -1");

            Packet answer = new Packet(PacketOperations.S2CMatchplayAckRelayConnection.getValue());
            answer.write((byte) 1);
            connection.sendTCP(answer);

            connection.close();
        }
    }
}
