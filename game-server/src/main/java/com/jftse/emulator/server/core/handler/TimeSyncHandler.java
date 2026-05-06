package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGServerTimeSync;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@PacketId(CMSGServerTimeSync.PACKET_ID)
public class TimeSyncHandler implements PacketHandler<FTConnection, CMSGServerTimeSync> {
    private static final Logger log = LogManager.getLogger("TimeSyncLogger");

    private static final long DIV = 20_000L;
    private static final long MAX_ABS_MS = 60_000L;
    private static final long MAX_REASONABLE_UNITS = MAX_ABS_MS / 2L;

    private static final long K = Long.divideUnsigned(-1L, DIV) + 1;

    private static final long UNDERFLOW_WINDOW_MS = 60_000L;
    private static final long UNDERFLOW_WINDOW_UNITS = UNDERFLOW_WINDOW_MS / 2L;

    private static final long CLOCK_WARN_MS = 10 * 60_000L; // 10 minutes
    private static final long STALL_WARN_MS = 5_000L;       // client "froze" >5s between syncs?

    @Override
    public void handle(FTConnection connection, CMSGServerTimeSync packet) {
        final String playerName = connection.getClient().getPlayer() != null ? connection.getClient().getPlayer().getName() : connection.getIPString();
        final long nowMs = System.currentTimeMillis();

        final long lastSentMs = connection.getLastTimeSyncSent();
        final long rttMs = (lastSentMs > 0) ? (nowMs - lastSentMs) : -1L;

        final long deltaUnits = packet.getSyncDelta();
        final String rawUnitsStr = Long.toUnsignedString(deltaUnits);

        if (packet.remaining() >= 16) {
            final long systemClockMs = packet.read(Long.class);
            final long steadyClockMs = packet.read(Long.class);

            final long prevSteadyClockMs = connection.getLastSteadyClockMs();
            final long prevRecvMs = connection.getLastTimeSyncRecv();

            connection.setLastSteadyClockMs(steadyClockMs);
            connection.setLastTimeSyncRecv(nowMs);

            final long wallDeltaMs = (lastSentMs > 0) ? (systemClockMs - lastSentMs) : 0L;
            final long offsetMs = (rttMs >= 0) ? (wallDeltaMs - (rttMs / 2)) : wallDeltaMs;

            Long steadyDriftMs = null;
            Long serverStepMs = null;
            if (prevSteadyClockMs > 0 && prevRecvMs > 0) {
                final long clientStep = steadyClockMs - prevSteadyClockMs;
                serverStepMs = nowMs - prevRecvMs;
                steadyDriftMs = clientStep - serverStepMs;
            }

            if (Math.abs(offsetMs) > CLOCK_WARN_MS) {
                log.warn("TimeSync ({}): large clock offset: {} ms (clientSysClock: {} ms, serverSentTime: {} ms, rtt: {} ms)", playerName, offsetMs, systemClockMs, lastSentMs, rttMs);
            } else {
                log.debug("TimeSync ({}): clock offset: {} ms (clientSysClock: {} ms, rtt: {} ms)", playerName, offsetMs, systemClockMs, rttMs);
            }

            if (steadyDriftMs != null) {
                if (Math.abs(steadyDriftMs) > STALL_WARN_MS) {
                    log.warn("TimeSync ({}): possible client stall: steady drift {} ms (serverStep: {} ms)", playerName, steadyDriftMs, serverStepMs);
                } else {
                    log.debug("TimeSync ({}): steady drift: {} ms (serverStep: {} ms)", playerName, steadyDriftMs, serverStepMs);
                }
            }

            return;
        }

        final long deltaMsSigned;
        if (Long.compareUnsigned(deltaUnits, MAX_REASONABLE_UNITS) <= 0) {
            deltaMsSigned = deltaUnits * 2L;
        } else {
            final long distToK = K - deltaUnits;
            if (distToK >= 0 && distToK <= UNDERFLOW_WINDOW_UNITS) {
                deltaMsSigned = -(distToK * 2L);
            } else {
                log.warn("TimeSync ({}): unreasonable delta units: {} (raw: {}, rtt: {} ms)", playerName, deltaUnits, rawUnitsStr, rttMs);
                return;
            }
        }

        if (Math.abs(deltaMsSigned) > MAX_ABS_MS) {
            log.warn("TimeSync ({}): unreasonable delta: {} ms (raw units: {})", playerName, deltaMsSigned, rawUnitsStr);
            return;
        }

        final long offsetMs = (rttMs >= 0) ? (deltaMsSigned - (rttMs / 2)) : deltaMsSigned;

        log.debug("TimeSync ({}): delta: {} ms (raw units: {}), rtt: {} ms, offset: {} ms", playerName, deltaMsSigned, rawUnitsStr, rttMs, offsetMs);
    }
}
