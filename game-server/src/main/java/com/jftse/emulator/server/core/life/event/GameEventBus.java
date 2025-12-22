package com.jftse.emulator.server.core.life.event;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.server.core.life.script.ScriptContextHelper;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.service.ScriptStateService;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.*;

@Service
@Log4j2
public class GameEventBus {
    private static GameEventBus instance;

    private static final Logger scriptLogger = LogManager.getLogger("ScriptLogger");

    private static final Map<GameEventType, List<GameEventCallback>> eventListeners = new HashMap<>();

    @Autowired
    private ScriptStateService scriptStateService;

    @PostConstruct
    public void init() {
        log.info("Loading events...");
        registerEvents();
        log.info("Game events has been loaded.");

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static GameEventBus getInstance() {
        if (instance == null) {
            instance = new GameEventBus();
        }
        return instance;
    }

    public ScriptStateService getScriptStateService() {
        return scriptStateService;
    }

    public void on(String eventType, GameEventCallback listener) {
        GameEventType type = GameEventType.valueOf(eventType.toUpperCase());
        eventListeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        log.info("Registered event for {}", type.getName());
    }

    public static void call(GameEventType eventType, Object... args) {
        final List<GameEventCallback> list = eventListeners.getOrDefault(eventType, List.of());
        for (GameEventCallback callback : list) {
            try {
                callback.onEvent(args);
            } catch (Exception e) {
                log.error("[{}] Error while processing event: {}", eventType.getName(), e.getMessage(), e);
            }
        }
    }

    public void call(String eventType, Object... args) {
        GameEventType type = GameEventType.valueOf(eventType.toUpperCase());
        call(type, args);
    }

    private boolean registerEvents() {
        Optional<ScriptManager> scriptManager = GameManager.getInstance().getScriptManager();
        boolean isError = false;
        if (scriptManager.isPresent()) {
            ScriptManager sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("EVENT");

            int count = 0;
            for (ScriptFile scriptFile : scriptFiles) {
                try {
                    Bindings bindings = sm.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("gameManager", GameManager.getInstance());
                    bindings.put("serviceManager", GameManager.getInstance().getServiceManager());
                    bindings.put("threadManager", GameManager.getInstance().getThreadManager());
                    bindings.put("eventHandler", GameManager.getInstance().getEventHandler());
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
        eventListeners.clear();
        boolean result = registerEvents();
        log.info("Game events reloaded.");
        return result;
    }
}
