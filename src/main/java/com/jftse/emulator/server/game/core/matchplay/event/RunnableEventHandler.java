package com.jftse.emulator.server.game.core.matchplay.event;

import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
@Getter
@Setter
public class RunnableEventHandler {
    public void handleQueuedRunnableEvents(GameSession gameSession) {
        long currentTime = Instant.now().toEpochMilli();

        // handle runnable events in queue
        List<RunnableEvent> runnableEventList = new ArrayList<>(gameSession.getRunnableEvents());
        for (int i = 0; i < runnableEventList.size(); i++) {
            RunnableEvent runnableEvent = runnableEventList.get(i);
            if (!runnableEvent.isFired() && runnableEvent.shouldFire(currentTime)) {
                runnableEvent.fire();
                gameSession.getRunnableEvents().remove(runnableEvent);
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