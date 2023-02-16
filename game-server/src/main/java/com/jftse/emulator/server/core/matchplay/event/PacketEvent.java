package com.jftse.emulator.server.core.matchplay.event;

import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketEvent {
    private FTConnection sender;
    private FTClient client;
    private Packet packet;
    private long packetTimestamp;
    private PacketEventType packetEventType; // unused as of current state, might be useful later on
    private long eventFireTime;
    private boolean fired = false;

    public void fire() {
        fired = true;
        sender.sendTCP(packet);
    }

    public boolean shouldFire(long currentTime) {
        return currentTime - packetTimestamp > eventFireTime;
    }
}