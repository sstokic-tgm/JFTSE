package com.jftse.emulator.server.game.core.matchplay.event;

import com.jftse.emulator.server.game.core.constants.PacketEventType;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Scope("singleton")
@Getter
@Setter
public class PacketEventHandler {
    private CopyOnWriteArrayList<PacketEvent> server_packetEventList;
    private CopyOnWriteArrayList<PacketEvent> client_packetEventList;

    public enum ServerClient {
        SERVER, CLIENT
    }

    @PostConstruct
    public void init() {
        server_packetEventList = new CopyOnWriteArrayList<>();
        client_packetEventList = new CopyOnWriteArrayList<>();
    }

    /**
     * Appends packetEvent to the end of this list.
     *
     * @param packetEvent
     */
    public void push(PacketEvent packetEvent, ServerClient serverClient) {
        if (serverClient == ServerClient.SERVER)
            server_packetEventList.add(packetEvent);
        else
            client_packetEventList.add(packetEvent);
    }

    /**
     * Removes and returns the first packetEvent from the list.
     * Shifts any subsequent elements to the left.
     *
     * @return removed first packetEvent
     */
    public PacketEvent pop(ServerClient serverClient) {
        if (serverClient == ServerClient.SERVER)
            return server_packetEventList.remove(0);
        else
            return client_packetEventList.remove(0);
    }

    /**
     * Removes the first occurrence of packetEvent from this list, if it is present.
     * Shifts any subsequent elements to the left.
     *
     * @param packetEvent to be removed from this list, if present
     */
    public void remove(PacketEvent packetEvent, ServerClient serverClient) {
        if (serverClient == ServerClient.SERVER)
            server_packetEventList.remove(packetEvent);
        else
            client_packetEventList.remove(packetEvent);
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left.
     *
     * @param index the index of the element to be removed
     */
    public void remove(int index, ServerClient serverClient) {
        if (serverClient == ServerClient.SERVER)
            server_packetEventList.remove(index);
        else
            client_packetEventList.remove(index);
    }

    public void handleQueuedPackets() {
        long currentTime = Instant.now().toEpochMilli();

        // handle server packets in queue
        for (int i = 0; i < server_packetEventList.size(); i++) {
            PacketEvent packetEvent = server_packetEventList.get(i);
            if (!packetEvent.isFired() && packetEvent.shouldFire(currentTime)) {
                if (packetEvent.getSender().isConnected())
                    packetEvent.fire();

                this.remove(i, PacketEventHandler.ServerClient.SERVER);
            }
        }
    }

    public PacketEvent createPacketEvent(Client client, Packet packet, PacketEventType packetEventType, long eventFireTime) {
        long packetTimestamp = Instant.now().toEpochMilli();

        PacketEvent packetEvent = new PacketEvent();
        packetEvent.setSender(client.getConnection());
        packetEvent.setClient(client);
        packetEvent.setPacket(packet);
        packetEvent.setPacketTimestamp(packetTimestamp);
        packetEvent.setPacketEventType(packetEventType);
        packetEvent.setEventFireTime(eventFireTime);

        return packetEvent;
    }
}