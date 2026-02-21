package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGServerTimeSync;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGServerTimeSync.PACKET_ID)
public class TimeSyncHandler implements PacketHandler<FTConnection, CMSGServerTimeSync> {
    @Override
    public void handle(FTConnection connection, CMSGServerTimeSync packet) {
        final String playerName = connection.getClient().getPlayer() != null ? connection.getClient().getPlayer().getName() : connection.getIPString();
        final long nowMs = System.currentTimeMillis();

        long syncDelta = packet.getSyncDelta();
        long syncDeltaMs = packet.getSyncDelta() * 2L; // 2ms per unit

        final long sentMs = connection.getLastTimeSyncSent();
        final long rtt = (sentMs > 0) ? (nowMs - sentMs) : -1L;
        final long skewMs = syncDeltaMs - (rtt / 2);

        if (syncDeltaMs < 0 || syncDeltaMs > 60_000) {
            log.warn("TimeSync ({}): Received invalid sync delta: {} ({} ms), skew: {} ms", playerName, syncDelta, syncDeltaMs, skewMs);
            return;
        }

        log.debug("TimeSync ({}): delta: {} ({} ms), RTT: {} ms, skew: {} ms", playerName, syncDelta, syncDeltaMs, rtt, skewMs);
    }
}
