package com.jftse.emulator.server.core.matchplay.event;

import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Getter
@Setter
@Log4j2
public class EventHandler {
    private ConcurrentLinkedDeque<Fireable> fireableDeque;

    private final Object dequeLock = new Object();

    @PostConstruct
    public void init() {
        fireableDeque = new ConcurrentLinkedDeque<>();
    }

    /**
     * Appends packetEvent to the end of this list.
     *
     * @param fireable
     */
    public void push(Fireable fireable) {
        fireableDeque.push(fireable);
    }

    /**
     * Removes and returns the first fireable from the list.
     * Shifts any subsequent elements to the left.
     *
     * @return removed first fireable
     */
    public Fireable pop() {
        return fireableDeque.pop();
    }

    public Fireable peek() {
        return fireableDeque.peek();
    }

    public Fireable poll() {
        return fireableDeque.poll();
    }

    /**
     * Removes the first occurrence of packetEvent from this list, if it is present.
     * Shifts any subsequent elements to the left.
     *
     * @param fireable to be removed from this list, if present
     */
    public void remove(Fireable fireable) {
        fireableDeque.remove(fireable);
    }

    public void handleQueuedEvents() {
        long currentTime = Instant.now().toEpochMilli();

        // handle fireables
        synchronized (dequeLock) {
            Iterator<Fireable> it = fireableDeque.iterator();
            while (it.hasNext()) {
                Fireable fireable = it.next();
                if (!fireable.isFired() && fireable.shouldFire(currentTime)) {
                    fireable.fire();
                    it.remove();
                    log.info("Fired fireable: " + fireable.getSelf().getClass().getSimpleName());
                }
                if (fireable.isCancelled()) {
                    it.remove();
                    log.info("Removed cancelled fireable: " + fireable.getSelf().getClass().getSimpleName());
                }
            }
        }
    }

    public PacketEvent createPacketEvent(FTClient client, Packet packet, PacketEventType packetEventType, long eventFireTime) {
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

    public RunnableEvent createRunnableEvent(Runnable runnable, long eventFireTime) {
        long packetTimestamp = Instant.now().toEpochMilli();

        RunnableEvent runnableEvent = new RunnableEvent();
        runnableEvent.setRunnable(runnable);
        runnableEvent.setRunnableTimeStamp(packetTimestamp);
        runnableEvent.setEventFireTime(eventFireTime);

        return runnableEvent;
    }
}