package com.jftse.emulator.server.game.core.matchplay.event;

import com.jftse.emulator.server.game.core.constants.PacketEventType;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacketEvent {
    private Connection sender;
    private Client client;
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