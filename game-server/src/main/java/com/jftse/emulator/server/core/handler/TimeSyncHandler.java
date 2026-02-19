package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGTimeSyncResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGTimeSyncResponse.PACKET_ID)
public class TimeSyncHandler implements PacketHandler<FTConnection, CMSGTimeSyncResponse> {
    @Override
    public void handle(FTConnection connection, CMSGTimeSyncResponse packet) {
        final String playerName = connection.getClient().getPlayer() != null ? connection.getClient().getPlayer().getName() : connection.getIPString();
        final long nowMs = System.currentTimeMillis();

        long syncDeltaUnits = packet.getSyncDelta(); // 2ms per unit
        long syncDeltaMs = syncDeltaUnits * 2L;

        final long sentMs = connection.getLastTimeSyncSent();
        final long rtt = (sentMs > 0) ? (nowMs - sentMs) : -1L;

        if (syncDeltaMs < 0 || syncDeltaMs > 60_000) {
            log.warn("({}) Received invalid time sync delta: {} units ({} ms), RTT: {} ms", playerName, syncDeltaUnits, syncDeltaMs, rtt);
            return;
        }

        log.debug("({}) Sync delta: {} units ({} ms), RTT: {} ms", playerName, syncDeltaUnits, syncDeltaMs, rtt);
    }
}
