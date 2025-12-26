package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.*;
import com.jftse.server.core.shared.MetricsService;
import com.jftse.server.core.util.Time;
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

    private final static int MAX_PROCESSED_PACKETS_PER_UPDATE = 3;

    private final MetricsService metrics;

    public FTConnection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        super(decryptionKey, encryptionKey, serverType);
        this.metrics = ServiceManager.getInstance().getMetricsService();
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

            final IPacket packet = recvQueue.poll();
            if (packet == null)
                continue;

            final long updateStartTime = Time.getNSTime();
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
            final long updateTime = Time.nanoToMillis(Time.getNSTimeDiff(updateStartTime, Time.getNSTime()));

            // track avg per packet id
            metrics.average("packet_process_time." + Integer.toHexString(packet.getPacketId()), updateTime, ServerType.AUTH_SERVER);

            processedPackets++;
        }

        return !getIsClosingConnection().get();
    }
}
