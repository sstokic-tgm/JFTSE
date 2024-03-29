package com.jftse.emulator.server.core.life.event;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.server.core.interaction.GameEventScriptable;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.event.SGameEvent;
import com.jftse.server.core.service.GameEventService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class GameEventRegistry {
    private static GameEventRegistry instance;

    private ConcurrentHashMap<String, List<GameEventMetadata>> registeredEvents;

    @Autowired
    private GameEventService gameEventService;

    @PostConstruct
    public void init() {
        instance = this;

        registeredEvents = new ConcurrentHashMap<>();

        log.info("Loading events...");
        registerEvents();
        log.info("Game events has been loaded.");

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static GameEventRegistry getInstance() {
        return instance;
    }

    public void registerEvent(ScriptFile scriptFile, GameEventScriptable event, boolean enabled) {
        GameEventMetadata gameEventMetadata = new GameEventMetadata(scriptFile.getId(), event.getName(), event.getType(), event.getDesc(), enabled, event);

        Optional<SGameEvent> optGameEvent = gameEventService.findById(scriptFile.getId());
        if (optGameEvent.isEmpty()) {
            SGameEvent gameEvent = new SGameEvent();
            gameEvent.setId(scriptFile.getId());
            gameEvent.setName(event.getName());
            gameEvent.setType(event.getType());
            gameEvent.setDescription(event.getDesc());
            gameEvent.setEnabled(enabled);
            gameEventService.save(gameEvent);
        }

        registeredEvents.computeIfAbsent(event.getType(), m -> new ArrayList<>()).add(gameEventMetadata);
        log.info("Registered event: " + event.getName() + " (" + event.getType() + ")");
    }

    private void registerEvents() {
        Optional<ScriptManager> scriptManager = GameManager.getInstance().getScriptManager();
        if (scriptManager.isPresent()) {
            ScriptManager sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("EVENT");
            for (ScriptFile scriptFile : scriptFiles) {
                try {
                    Bindings bindings = sm.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("gameManager", GameManager.getInstance());
                    bindings.put("serviceManager", GameManager.getInstance().getServiceManager());
                    bindings.put("eventRegistry", this);

                    GameEventScriptable event = sm.getInterfaceByImplementingObject(scriptFile, "impl", GameEventScriptable.class, bindings);
                    registerEvent(scriptFile, event, isEventEnabled(scriptFile.getId()));
                } catch (Exception e) {
                    log.error("Error on register event from script: " + scriptFile.getFile().getName().split("_")[1].split("\\.")[0] + ". ScriptException: " + e.getMessage());
                }
            }
        }
    }

    private boolean isEventEnabled(Long id) {
        Optional<SGameEvent> gameEvent = gameEventService.findById(id);
        if (gameEvent.isEmpty()) {
            return false;
        }
        return gameEvent.get().getEnabled();
    }

    private boolean isEventEnabled(SGameEvent gameEvent) {
        if (gameEvent == null) {
            return false;
        }
        return gameEvent.getEnabled();
    }

    public boolean isEventValid(Long id) {
        Optional<SGameEvent> gameEvent = gameEventService.findById(id);
        if (gameEvent.isPresent()) {
            SGameEvent ge = gameEvent.get();

            if (isEventEnabled(ge)) {
                Date now = new Date();

                Date startDate = ge.getStartDate();
                Date endDate = ge.getEndDate();
                if (startDate != null && endDate != null) {
                    int afterStartDt = now.compareTo(startDate);
                    int beforeEndDt = now.compareTo(endDate);
                    return afterStartDt >= 0 && beforeEndDt <= 0;
                } else if (startDate != null) {
                    int afterStartDt = now.compareTo(startDate);
                    return afterStartDt >= 0;
                } else if (endDate != null) {
                    int beforeEndDt = now.compareTo(endDate);
                    return beforeEndDt <= 0;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void enableEvent(Long id) {
        GameEventMetadata eventMetadata = getEventMetaDataById(id);
        if (eventMetadata != null) {
            eventMetadata.setEnabled(true);
            Optional<SGameEvent> gameEvent = gameEventService.findById(id);
            if (gameEvent.isPresent()) {
                SGameEvent ge = gameEvent.get();
                ge.setEnabled(true);
                gameEventService.save(ge);
            }
        }
    }

    public void disableEvent(Long id) {
        GameEventMetadata eventMetadata = getEventMetaDataById(id);
        if (eventMetadata != null) {
            eventMetadata.setEnabled(false);
            Optional<SGameEvent> gameEvent = gameEventService.findById(id);
            if (gameEvent.isPresent()) {
                SGameEvent ge = gameEvent.get();
                ge.setEnabled(false);
                gameEventService.save(ge);
            }
        }
    }

    private GameEventMetadata getEventMetaDataById(Long id) {
        for (Map.Entry<String, List<GameEventMetadata>> entry : registeredEvents.entrySet()) {
            for (GameEventMetadata event : entry.getValue()) {
                if (event.getId().equals(id)) {
                    return event;
                }
            }
        }
        return null;
    }

    public void triggerEventWithType(final GameEventType eventType, final FTClient client, Object... args) {
        final List<GameEventMetadata> eventMetadataList = registeredEvents.getOrDefault(eventType.getName(), Collections.emptyList());
        for (GameEventMetadata eventMetadata : eventMetadataList) {
            if (eventMetadata.isEnabled() && isEventValid(eventMetadata.getId())) {
                eventMetadata.getEvent().onEvent(client, args);
            }
        }
    }
}
