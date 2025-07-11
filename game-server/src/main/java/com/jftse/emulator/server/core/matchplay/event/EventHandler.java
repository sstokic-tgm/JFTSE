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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Getter
@Setter
@Log4j2
public class EventHandler {
    private BlockingQueue<Fireable> fireableDeque;
    private final Lock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        fireableDeque = new LinkedBlockingQueue<>();
    }

    /**
     * Appends packetEvent to the end of this list.
     *
     * @param fireable
     */
    public void offer(Fireable fireable) {
        fireableDeque.offer(fireable);
    }

    /**
     * Appends packetEvent to the end of this list.
     * This method is thread-safe and should be used only for calls from JavaScript code.
     *
     * @param fireable to be added to the queue
     */
    public void offerJS(Fireable fireable) {
        try {
            lock.lock();
            fireableDeque.offer(fireable);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue,
     * waiting if necessary until an element becomes available.
     *
     * @return removed first fireable
     */
    public Fireable take() throws InterruptedException {
        return fireableDeque.take();
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

        try {
            Fireable fireable = take();
            if (fireable != null) {
                if (!fireable.isFired() && fireable.shouldFire(currentTime) && !fireable.isCancelled()) {
                    fireable.fire();
                    log.info("Fired fireable: {}", fireable.getSelf().getClass().getSimpleName());
                } else if (!fireable.isCancelled()) {
                    offer(fireable);
                }
            }
            // save cpu cycles
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Event handler interrupted: {}", e.getMessage());
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
        try {
            lock.lock();

            long packetTimestamp = Instant.now().toEpochMilli();
            RunnableEvent runnableEvent = new RunnableEvent();
            runnableEvent.setRunnable(runnable);
            runnableEvent.setRunnableTimeStamp(packetTimestamp);
            runnableEvent.setEventFireTime(eventFireTime);
            return runnableEvent;
        } finally {
            lock.unlock();
        }
    }
}