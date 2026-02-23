package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGServerTimeSync;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGServerTimeSync.PACKET_ID)
public class TimeSyncHandler implements PacketHandler<FTConnection, CMSGServerTimeSync> {
    private static final long DIV = 20_000L;
    private static final long MAX_ABS_MS = 60_000L;
    private static final long MAX_REASONABLE_UNITS = MAX_ABS_MS / 2;

    private static final long K = Long.divideUnsigned(-1L, DIV) + 1;

    @Override
    public void handle(FTConnection connection, CMSGServerTimeSync packet) {
        final String playerName = connection.getClient().getPlayer() != null ? connection.getClient().getPlayer().getName() : connection.getIPString();
        final long nowMs = System.currentTimeMillis();
        final long lastSentMs = connection.getLastTimeSyncSent();

        final long deltaUnits = packet.getSyncDelta();
        final String rawUnitsStr = Long.toUnsignedString(deltaUnits);

        final long deltaMsSigned;
        if (Long.compareUnsigned(deltaUnits, MAX_REASONABLE_UNITS) <= 0) {
            deltaMsSigned = deltaUnits * 2L;
        } else {
            final long signedUnits = deltaUnits - K;
            deltaMsSigned = signedUnits * 2L;
        }

        if (Math.abs(deltaMsSigned) > MAX_ABS_MS) {
            log.warn("TimeSync ({}): unreasonable delta: {} ms (raw units: {})", playerName, deltaMsSigned, rawUnitsStr);
            return;
        }

        final long rttMs = (lastSentMs > 0) ? (nowMs - lastSentMs) : -1L;
        final long skewMs = (rttMs >= 0) ? (deltaMsSigned - (rttMs / 2)) : 0L;

        log.debug("TimeSync ({}): delta: {} ms (raw units: {}), rtt: {} ms, skew: {} ms", playerName, deltaMsSigned, rawUnitsStr, rttMs, skewMs);
    }
}
