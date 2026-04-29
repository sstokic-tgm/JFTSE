package com.jftse.emulator.common.scripting;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IncludeFunction implements ProxyExecutable {
    private final ScriptFile owner;
    private final Context context;
    private final Map<String, ScriptFile> libraries;
    private final Set<String> loaded = ConcurrentHashMap.newKeySet();

    public IncludeFunction(ScriptFile owner, Context context, Map<String, ScriptFile> libraries) {
        this.owner = owner;
        this.context = context;
        this.libraries = libraries;
    }

    @Override
    public Object execute(Value... arguments) {
        if (arguments.length < 1 || !arguments[0].isString()) {
            throw new IllegalArgumentException("include(...) expects a library path string");
        }

        include(arguments[0].asString());
        return null;
    }

    private void include(String key) {
        String normalizedKey = normalizeIncludeKey(key);

        if (!loaded.add(normalizedKey)) {
            return;
        }

        ScriptFile library = libraries.get(normalizedKey);
        if (library == null) {
            loaded.remove(normalizedKey);
            throw new IllegalArgumentException(
                    "Library not found: " + key + " requested by " + owner.getScriptKey()
            );
        }

        context.eval(library.getSource());
    }

    private String normalizeIncludeKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Include key must not be empty");
        }

        String normalized = key.trim()
                .replace("\\", "/");

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.startsWith("LIB/")) {
            normalized = normalized.substring(4);
        } else if (normalized.startsWith("lib/")) {
            normalized = normalized.substring(4);
        }

        if (normalized.endsWith(".js")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }

        if (normalized.contains("..")) {
            throw new IllegalArgumentException("Invalid include path: " + key);
        }

        return normalized.toLowerCase(Locale.ROOT);
    }
}
