package com.jftse.emulator.server.core.life.script;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.server.core.service.ScriptStateService;
import lombok.extern.log4j.Log4j2;

/**
 * Helper class to manage script context state.
 * It provides methods to get, set, and delete state variables,
 * both globally and per account, as well as JSON serialization support.
 *
 * Usage:
 * <pre>
 *     ScriptContextHelper contextHelper = new ScriptContextHelper(stateService, scriptFile);
 *     contextHelper.set("variableName", "value");
 *     String value = contextHelper.get("variableName");
 * </pre>
 *
 * Usage within scripts:
 * <pre>
 *     let stateValue = state.get("variableName", accountId);
 *     state.set("variableName", accountId, "newValue");
 *
 *     // JSON example
 *     let jsonObject = state.getJson("jsonVariable", accountId, Object);
 *     state.setJson("jsonVariable", accountId, jsonObject);
 * </pre>
 */
@Log4j2
public class ScriptContextHelper {
    private final ScriptStateService stateService;
    private final String scriptId;

    /**
     * Constructs a ScriptContextHelper for the given script file.
     *
     * @param stateService the script state service
     * @param scriptFile   the script file object
     */
    public ScriptContextHelper(ScriptStateService stateService, ScriptFile scriptFile) {
        this.stateService = stateService;

        final String type = StringUtils.isEmpty(scriptFile.getType()) ? "unknown" : scriptFile.getType().toLowerCase();
        final String id = String.valueOf(scriptFile.getId());
        this.scriptId = StringUtils.isEmpty(scriptFile.getSubType())
                ? String.format("%s:%s:%s", type, id, scriptFile.getName())
                : String.format("%s-%s:%s:%s", type, scriptFile.getSubType().toLowerCase(), id, scriptFile.getName());
        log.debug(scriptId);
    }

    /**
     * Gets the value of a state variable by name.
     *
     * @param name the name of the state variable
     * @return the value of the state variable, or null if not found
     */
    public String get(String name) {
        return stateService.get(scriptId, name).orElse(null);
    }

    /**
     * Gets the value of a state variable by name for a specific account.
     *
     * @param name      the name of the state variable
     * @param accountId the account ID
     * @return the value of the state variable, or null if not found
     */
    public String get(String name, Long accountId) {
        return stateService.get(scriptId, name, accountId).orElse(null);
    }

    /**
     * Sets the value of a state variable.
     *
     * @param name the name of the state variable
     * @param value the value to set
     */
    public void set(String name, String value) {
        stateService.set(scriptId, name, value);
    }

    /**
     * Sets the value of a state variable for a specific account.
     *
     * @param name the name of the state variable
     * @param accountId the account ID
     * @param value the value to set
     */
    public void set(String name, Long accountId, String value) {
        stateService.set(scriptId, name, accountId, value);
    }

    /**
     * Deletes a state variable by name.
     *
     * @param name the name of the state variable
     */
    public void delete(String name) {
        stateService.delete(scriptId, name);
    }

    /**
     * Deletes a state variable by name for a specific account.
     *
     * @param name the name of the state variable
     * @param accountId the account ID
     */
    public void delete(String name, Long accountId) {
        stateService.delete(scriptId, name, accountId);
    }

    /**
     * Gets a JSON-serialized state variable by name.
     *
     * @param name  the name of the state variable
     * @param clazz the class of the expected return type
     * @param <T>   the type of the expected return value
     * @return the deserialized object, or null if not found
     */
    public <T> T getJson(String name, Class<T> clazz) {
        return stateService.getJson(scriptId, name, clazz);
    }

    /**
     * Gets a JSON-serialized state variable by name for a specific account.
     *
     * @param name      the name of the state variable
     * @param accountId the account ID
     * @param clazz     the class of the expected return type
     * @param <T>       the type of the expected return value
     * @return the deserialized object, or null if not found
     */
    public <T> T getJson(String name, Long accountId, Class<T> clazz) {
        return stateService.getJson(scriptId, name, accountId, clazz);
    }

    /**
     * Sets a JSON-serialized state variable.
     *
     * @param name  the name of the state variable
     * @param value the object to serialize and store
     * @param <T>   the type of the object to store
     */
    public <T> void setJson(String name, T value) {
        try {
            stateService.setJson(scriptId, name, value);
        } catch (ValidationException e) {
            log.error("Failed to set JSON value for script {}: {}", scriptId, e.getMessage(), e);
        }
    }

    /**
     * Sets a JSON-serialized state variable for a specific account.
     *
     * @param name      the name of the state variable
     * @param accountId the account ID
     * @param value     the object to serialize and store
     * @param <T>       the type of the object to store
     */
    public <T> void setJson(String name, Long accountId, T value) {
        try {
            stateService.setJson(scriptId, name, accountId, value);
        } catch (ValidationException e) {
            log.error("Failed to set JSON value for script {}: {}", scriptId, e.getMessage(), e);
        }
    }
}
