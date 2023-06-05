package com.jftse.emulator.common.scripting;

import lombok.extern.log4j.Log4j2;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Log4j2
public class ScriptManager {
    private static ScriptManager instance;

    private ScriptEngineManager scriptEngineManager;
    private ScriptEngine scriptEngine;

    private final Object lock = new Object();

    private final ConcurrentHashMap<String, List<ScriptFile>> scripts = new ConcurrentHashMap<>();
    public final static List<String> allowedTypes = Arrays.asList("EVENT", "QUEST", "COMMAND");

    private ScriptManager() {
        initialize(new ArrayList<>());
    }

    public ScriptManager(List<ScriptFile> scriptFilesList) {
        initialize(scriptFilesList);
        instance = this;
    }

    public static ScriptManager getInstance() {
        return instance == null ? instance = new ScriptManager() : instance;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    private void initialize(List<ScriptFile> scriptFilesList) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        scriptFilesList.removeIf(scriptFile -> !hasScriptValidType(scriptFile.getType()));

        scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("js");
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);

        Compilable compilable = (Compilable) scriptEngine;

        FileReader fileReader;
        for (ScriptFile scriptFile : scriptFilesList) {
            try {
                fileReader = new FileReader(scriptFile.getFile());
                CompiledScript compiledScript = compilable.compile(fileReader);

                scriptFile.setCompiledScript(compiledScript);

                List<ScriptFile> tmpScriptFileList;
                if (!scripts.containsKey(scriptFile.getType())) {
                    tmpScriptFileList = new ArrayList<>();
                } else {
                    tmpScriptFileList = scripts.get(scriptFile.getType());
                }
                synchronized (lock) {
                    tmpScriptFileList.add(scriptFile);
                }
                scripts.put(scriptFile.getType(), tmpScriptFileList);
            } catch (FileNotFoundException e) {
                log.error("Creating FileReader error: " + e.getMessage(), e);
            } catch (ScriptException e) {
                log.error("Error on compiling script: " + e.getMessage(), e);
            }
        }
    }

    private boolean hasScriptValidType(String type) {
        return allowedTypes.stream().anyMatch(t -> t.equalsIgnoreCase(type));
    }

    public List<ScriptFile> getScriptFiles(String type) {
        return scripts.get(type);
    }

    public ConcurrentHashMap<String, List<ScriptFile>> getScripts() {
        return scripts;
    }

    public Optional<ScriptFile> getScriptFile(String type, Long id) {
        Optional<List<ScriptFile>> optionalScriptFileList = Optional.ofNullable(scripts.get(type));
        return optionalScriptFileList.flatMap(scriptFiles -> scriptFiles.stream().filter(scriptFile -> scriptFile.getId() != null && scriptFile.getId().equals(id)).findFirst());
    }

    public Object eval(ScriptFile scriptFile) throws ScriptException {
        CompiledScript compiledScript = scriptFile.getCompiledScript();
        return compiledScript.eval();
    }

    public Object eval(ScriptFile scriptFile, Bindings bindings) throws ScriptException {
        CompiledScript compiledScript = scriptFile.getCompiledScript();
        return compiledScript.eval(bindings);
    }

    public Object eval(ScriptFile scriptFile, ScriptContext context) throws ScriptException {
        CompiledScript compiledScript = scriptFile.getCompiledScript();
        return compiledScript.eval(context);
    }

    public <T> T getInterfaceByImplementingObject(ScriptFile scriptFile, String key, Class<T> interfaceClass) throws ScriptException {
        eval(scriptFile);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(classObject, interfaceClass);
    }

    public <T> T getInterfaceByImplementingObject(ScriptFile scriptFile, String key, Class<T> interfaceClass, Bindings bindings) throws ScriptException {
        eval(scriptFile, bindings);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(classObject, interfaceClass);
    }

    public <T> T getInterfaceByImplementingObject(ScriptFile scriptFile, String key, Class<T> interfaceClass, ScriptContext context) throws ScriptException {
        eval(scriptFile, context);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(classObject, interfaceClass);
    }

    public <T> T getInterface(ScriptFile scriptFile, Class<T> interfaceClass) throws ScriptException {
        eval(scriptFile);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(interfaceClass);
    }

    public <T> T getInterface(ScriptFile scriptFile, Class<T> interfaceClass, Bindings bindings) throws ScriptException {
        eval(scriptFile, bindings);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(interfaceClass);
    }

    public <T> T getInterface(ScriptFile scriptFile, Class<T> interfaceClass, ScriptContext context) throws ScriptException {
        eval(scriptFile, context);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.getInterface(interfaceClass);
    }

    public Object invokeFunction(ScriptFile scriptFile, String name, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(name, args);
    }

    public Object invokeFunction(ScriptFile scriptFile, String name, Bindings bindings, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile, bindings);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(name, args);
    }

    public Object invokeFunction(ScriptFile scriptFile, String name, ScriptContext context, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile, context);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(name, args);
    }

    public Object invokeMethod(ScriptFile scriptFile, String key, String name, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeMethod(classObject, name, args);
    }

    public Object invokeMethod(ScriptFile scriptFile, String key, String name, Bindings bindings, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile, bindings);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeMethod(classObject, name, args);
    }

    public Object invokeMethod(ScriptFile scriptFile, String key, String name, ScriptContext context, Object... args) throws ScriptException, NoSuchMethodException {
        eval(scriptFile, context);

        Object classObject = scriptEngine.get(key);

        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeMethod(classObject, name, args);
    }
}
