package com.jftse.emulator.server.core.matchplay.event;

import com.jftse.emulator.server.core.constants.PacketEventType;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.util.GameTime;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Getter
@Setter
@Log4j2
public class EventHandler {
    private BlockingQueue<Fireable> fireableDeque;

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
     * This method should be used only for fireables created in JS context
     * that mutate JS state.
     *
     * @param fireable to be added to the queue
     */
    public void offerJS(Fireable fireable) {
        fireable.setExecutionMode(ExecutionMode.JS_INLINE);
        fireableDeque.offer(fireable);
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
     * Retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return removed first fireable or null
     */
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
        long now = GameTime.getGameTimeMS();
        List<Fireable> snapshot = new ArrayList<>();
        fireableDeque.drainTo(snapshot);

        for (Fireable fireable : snapshot) {
            if (fireable.isCancelled() || fireable.isFired()) {
                continue;
            }

            if (fireable.shouldFire(now)) {
                fireable.fire();
            } else {
                fireableDeque.offer(fireable);
            }
        }
    }

    public PacketEvent createPacketEvent(FTClient client, Packet packet, PacketEventType packetEventType, long delayMS) {
        return PacketEvent.builder()
                        .sender(client.getConnection())
                        .client(client)
                        .packet(packet)
                        .packetEventType(packetEventType)
                        .currentTime(GameTime.getGameTimeMS())
                        .delayMS(delayMS)
                        .build();
    }

    public RunnableEvent createRunnableEvent(Runnable runnable, long delayMS) {
        return RunnableEvent.builder()
                .runnable(runnable)
                .currentTime(GameTime.getGameTimeMS())
                .delayMS(delayMS)
                .build();
    }
}