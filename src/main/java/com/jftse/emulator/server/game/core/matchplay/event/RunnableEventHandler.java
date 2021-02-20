package com.jftse.emulator.server.game.core.matchplay.event;

import com.jftse.emulator.server.game.core.matchplay.room.GameSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Scope("singleton")
@Getter
@Setter
public class RunnableEventHandler {
    public void handleQueuedRunnableEvents(GameSession gameSession) {
        long currentTime = Instant.now().toEpochMilli();

        // handle runnable events in queue
        for (int i = 0; i < gameSession.getRunnableEvents().size(); i++) {
            RunnableEvent runnableEvent = gameSession.getRunnableEvents().get(i);
            if (!runnableEvent.isFired() && runnableEvent.shouldFire(currentTime)) {
                runnableEvent.fire();
                gameSession.getRunnableEvents().remove(i);
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