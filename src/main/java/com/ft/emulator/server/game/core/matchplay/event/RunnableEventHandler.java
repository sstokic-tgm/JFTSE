package com.ft.emulator.server.game.core.matchplay.event;

import com.ft.emulator.server.game.core.constants.PacketEventType;
import com.ft.emulator.server.game.core.matchplay.room.GameSession;
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