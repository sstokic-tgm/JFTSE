package com.ft.emulator.server.game.core.matchplay.event;

import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
public class RunnableEventHandler {
    private CopyOnWriteArrayList<RunnableEvent> runnnableEventList;

    @PostConstruct
    public void init() {
        runnnableEventList = new CopyOnWriteArrayList<>();
    }

    /**
     * Appends packetEvent to the end of this list.
     *
     * @param runnableEvent
     */
    public void push(RunnableEvent runnableEvent) {
        runnnableEventList.add(runnableEvent);
    }

    /**
     * Removes and returns the first packetEvent from the list.
     * Shifts any subsequent elements to the left.
     *
     * @return removed first packetEvent
     */
    public RunnableEvent pop() {
        return runnnableEventList.remove(0);
    }

    /**
     * Removes the first occurrence of packetEvent from this list, if it is present.
     * Shifts any subsequent elements to the left.
     *
     * @param runnableEvent to be removed from this list, if present
     */
    public void remove(RunnableEvent runnableEvent) {
        runnnableEventList.remove(runnableEvent);
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left.
     *
     * @param index the index of the element to be removed
     */
    public void remove(int index) {
        runnnableEventList.remove(index);
    }

    public void handleQueuedRunnableEvents() {
        long currentTime = Instant.now().toEpochMilli();

        // handle runnable events in queue
        for (int i = 0; i < runnnableEventList.size(); i++) {
            RunnableEvent runnableEvent = runnnableEventList.get(i);
            if (!runnableEvent.isFired() && runnableEvent.shouldFire(currentTime)) {
                runnableEvent.fire();
                this.remove(i);
            }
        }
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