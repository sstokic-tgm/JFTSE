package com.jftse.emulator.server.net;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
@Log4j2
public class FTConnection extends Connection<FTClient> {
    private String hwid;

    private ConcurrentLinkedQueue<IPacket> recvQueue = new ConcurrentLinkedQueue<>();

    private final static int MAX_PROCESSED_PACKETS_PER_UPDATE = 100;

    public FTConnection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        super(decryptionKey, encryptionKey, serverType);
    }

    public void queuePacket(IPacket packet) {
        recvQueue.add(packet);
    }

    public boolean update(long diff) {
        final FTClient client = getClient();
        int processedPackets = 0;

        while (!getIsClosingConnection().get()) {
            if (processedPackets >= MAX_PROCESSED_PACKETS_PER_UPDATE || recvQueue.isEmpty()) {
                break;
            }

            IPacket packet = recvQueue.poll();
            if (packet == null)
                continue;

            try {
                PacketHandler<FTConnection, IPacket> handler = PacketRegistry.getHandler(packet.getPacketId());
                if (handler != null) {
                    handler.handle(this, packet);
                } else {
                    log.warn("No handler for packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId());
                }
            } catch (Exception e) {
                log.error("Error processing packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId(), e);
            }

            processedPackets++;
        }

        if (processedPackets > 0) {
            String name = client != null && client.getAccount() != null ? client.getAccount().getUsername() : this.getRemoteAddressTCP().toString();
            log.debug("Processed {} packets for {}", processedPackets, name);
        }

        if (getIsClosingConnection().get())
            return false;

        return true;
    }
}
