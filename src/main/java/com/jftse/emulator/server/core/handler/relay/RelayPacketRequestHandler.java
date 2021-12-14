package com.jftse.emulator.server.core.handler.relay;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.handler.relay.spidermine.SpiderMineExplodeHandler;
import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;

@Log4j2
public class RelayPacketRequestHandler extends AbstractHandler {
    private Packet relayPacket;

    @Override
    public boolean process(Packet packet) {
        relayPacket = new Packet(packet.getData());
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() != null) {
            GameSession gameSession = connection.getClient().getActiveGameSession();
            if (gameSession != null) {
                Class<? extends AbstractHandler> handlerClass = PacketOperations.nullableHandlerOf(relayPacket.getPacketId());

                if (handlerClass != null) {
                    try {
                        AbstractHandler handler = handlerClass.getDeclaredConstructor().newInstance();
                        handler.setConnection(connection);
                        if (handler.process(relayPacket)) {
                            handler.handle();

                            if (handlerClass == PacketOperations.C2CSpiderMineExplodePacket.getHandler()) {
                                log.debug("packet already should be relayed customly, don't relay the packet again");
                                return;
                            }
                        }

                    } catch (Exception e) {
                        connection.notifyException(e);
                    }
                }

                ConcurrentLinkedDeque<Client> clientList = new ConcurrentLinkedDeque<>(gameSession.getClientsInRelay());
                int clientsSize = clientList.size();
                for (int i = 0; i < clientsSize; i++) {
                    Client client = clientList.poll();

                    if (client.getConnection() != null && client.getConnection().isConnected()) {
                        client.getConnection().sendTCP(relayPacket);
                    }
                }
            }
        }
    }
}
