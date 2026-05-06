package com.jftse.emulator.server.core.life.event;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManagerV2;
import com.jftse.emulator.server.core.life.script.ScriptContextHelper;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.service.ScriptStateService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Log4j2
@AllArgsConstructor
public class GameEventBus {
    @Getter
    private static GameEventBus instance;

    private static final Logger scriptLogger = LogManager.getLogger("ScriptLogger");

    private static final Map<String, CopyOnWriteArrayList<GameEventCallback>> eventListeners = new ConcurrentHashMap<>();
    private static final Map<String, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
    private static final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();

    @Getter
    private final ScriptStateService scriptStateService;

    @PostConstruct
    public void init() {
        instance = this;

        log.info("Loading events...");
        registerEvents();
        log.info("Game events has been loaded.");

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public void on(String eventType, GameEventCallback listener) {
        String type = null;
        try {
            type = GameEventType.valueOf(eventType.toUpperCase()).getName();
        } catch (IllegalArgumentException e) {
            log.warn("Event {} not found in enum GameEventType. Custom Listener will be registered without enum type. Make sure to handle this event properly in your script.", eventType);
            type = eventType.toUpperCase();
        }

        eventListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        eventLocks.computeIfAbsent(type, k -> new ReentrantLock());
        log.info("Registered event for {}", type);
    }

    public static void call(GameEventType eventType, Object... args) {
        call0(eventType.getName(), args);
    }

    private static void call0(String eventType, Object... args) {
        lifecycleLock.readLock().lock();
        try {
            ReentrantLock lock = eventLocks.computeIfAbsent(eventType, k -> new ReentrantLock());
            lock.lock();
            try {
                List<GameEventCallback> listeners = eventListeners.get(eventType);
                if (listeners == null || listeners.isEmpty()) {
                    return;
                }

                for (GameEventCallback listener : listeners) {
                    try {
                        listener.onEvent(args);
                    } catch (Exception e) {
                        log.error("Error while executing event: {}. Exception: {}", eventType, e.getMessage(), e);
                    }
                }
            } finally {
                lock.unlock();
            }
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    public void call(String eventType, Object... args) {
        call0(eventType.toUpperCase(), args);
    }

    private boolean registerEvents() {
        Optional<ScriptManagerV2> scriptManager = GameManager.getInstance().getScriptManager();
        boolean isError = false;
        if (scriptManager.isPresent()) {
            ScriptManagerV2 sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("EVENT");

            int count = 0;
            for (ScriptFile scriptFile : scriptFiles) {
                try {
                    Map<String, Object> bindings = new HashMap<>();
                    bindings.put("gameManager", GameManager.getInstance());
                    bindings.put("serviceManager", GameManager.getInstance().getServiceManager());
                    bindings.put("threadManager", GameManager.getInstance().getThreadManager());
                    bindings.put("state", new ScriptContextHelper(scriptStateService, scriptFile));
                    bindings.put("geb", this);
                    bindings.put("log", scriptLogger);

                    sm.eval(scriptFile, bindings);
                    count++;
                } catch (Exception e) {
                    log.error("Error while evaluating event from script: {}. ScriptException: {}", scriptFile.getFile().getName().split("_")[1].split("\\.")[0], e.getMessage(), e);
                    isError = true;
                }
            }

            if (count > 0) {
                log.info("Loaded {} script files for events.", count);
            }
        }
        return !isError;
    }

    public boolean reloadEvents() {
        lifecycleLock.writeLock().lock();
        try {
            eventListeners.clear();
            boolean result = registerEvents();
            log.info("Game events reloaded.");
            return result;
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }
}
