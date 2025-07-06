package com.jftse.emulator.server.core.life.script;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.server.core.service.ScriptStateService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ScriptContextHelper {
    private final ScriptStateService stateService;
    private final String scriptId;

    public ScriptContextHelper(ScriptStateService stateService, ScriptFile scriptFile) {
        this.stateService = stateService;

        final String type = StringUtils.isEmpty(scriptFile.getType()) ? "unknown" : scriptFile.getType().toLowerCase();
        final String id = String.valueOf(scriptFile.getId());
        this.scriptId = StringUtils.isEmpty(scriptFile.getSubType())
                ? String.format("%s:%s:%s", type, id, scriptFile.getName())
                : String.format("%s-%s:%s:%s", type, scriptFile.getSubType().toLowerCase(), id, scriptFile.getName());
    }

    public String get(String name) {
        return stateService.get(scriptId, name).orElse(null);
    }

    public String get(Long accountId, String name) {
        return stateService.get(scriptId, accountId, name).orElse(null);
    }

    public void set(String name, String value) {
        stateService.set(scriptId, name, value);
    }

    public void set(Long accountId, String name, String value) {
        stateService.set(scriptId, accountId, name, value);
    }

    public void delete(String name) {
        stateService.delete(scriptId, name);
    }

    public void delete(Long accountId, String name) {
        stateService.delete(scriptId, accountId, name);
    }

    public <T> T getJson(String name, Class<T> clazz) {
        return stateService.getJson(scriptId, name, clazz);
    }

    public <T> T getJson(Long accountId, String name, Class<T> clazz) {
        return stateService.getJson(scriptId, accountId, name, clazz);
    }

    public <T> void setJson(String name, T value) {
        try {
            stateService.setJson(scriptId, name, value);
        } catch (ValidationException e) {
            log.error("Failed to set JSON value for script {}: {}", scriptId, e.getMessage(), e);
        }
    }

    public <T> void setJson(Long accountId, String name, T value) {
        try {
            stateService.setJson(scriptId, accountId, name, value);
        } catch (ValidationException e) {
            log.error("Failed to set JSON value for script {}: {}", scriptId, e.getMessage(), e);
        }
    }
}
