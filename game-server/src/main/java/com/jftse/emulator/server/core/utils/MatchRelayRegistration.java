package com.jftse.emulator.server.core.utils;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.shared.packets.matchplay.SMSGConnectMatchRelay;

import java.util.ArrayList;
import java.util.List;

public final class MatchRelayRegistration {
    private static final int REGISTRATION_SLOTS = 4;

    private MatchRelayRegistration() {
    }

    public static SMSGConnectMatchRelay create(String host, int port, int sessionId, FTClient recipient, List<FTClient> clients) {
        List<Integer> playerIds = new ArrayList<>(REGISTRATION_SLOTS);
        // The relay identifies this connection by the first ID, including spectators.
        playerIds.add(Math.toIntExact(recipient.getPlayer().getId()));
        clients.stream()
                .filter(c -> c != recipient && c.hasPlayer() && c.getRoomPlayer() != null)
                .filter(c -> c.getRoomPlayer().getPosition() >= 0 && c.getRoomPlayer().getPosition() < 4)
                .map(c -> Math.toIntExact(c.getPlayer().getId()))
                .filter(id -> !playerIds.contains(id))
                .limit(REGISTRATION_SLOTS - 1)
                .forEach(playerIds::add);
        while (playerIds.size() < REGISTRATION_SLOTS) {
            playerIds.add(0);
        }

        return SMSGConnectMatchRelay.builder()
                .host(host)
                .port((short) port)
                .sessionId(sessionId)
                .playerIds(playerIds)
                .build();
    }
}
