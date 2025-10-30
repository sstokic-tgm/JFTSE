package com.jftse.server.core.shared;

import com.jftse.emulator.common.utilities.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
@Log4j2
@Order(-1)
public class ServerConfService {
    private static final String CONF_FILE = "server.conf";

    private final Properties props = new Properties();
    private boolean loaded = false;

    @PostConstruct
    public  void init() {
        loaded = loadConf(true);
    }

    public boolean loadConf(boolean reload) {
        if (!reload) {
            return loaded;
        }

        Path execDir = getExecDir();
        Path confFile = execDir.resolve(CONF_FILE);
        if (!Files.exists(confFile)) {
            log.error("Configuration file not found: {}", confFile.toString());
            return false;
        }

        try (var reader = Files.newBufferedReader(confFile)) {
            props.clear();
            props.load(reader);
            return true;
        } catch (Exception e) {
            log.error("Failed to read configuration {}", confFile, e);
            return false;
        }
    }

    private Path getExecDir() {
        final String confPath = System.getProperty("conf.path");
        if (!StringUtils.isEmpty(confPath)) {
            return Paths.get(confPath).toAbsolutePath().normalize();
        } else {
            return Path.of(".").toAbsolutePath().normalize();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        Object castedValue = switch (defaultValue) {
            case Integer i -> Integer.parseInt(value);
            case Long l -> Long.parseLong(value);
            case Boolean b -> Boolean.parseBoolean(value);
            case Float f -> Float.parseFloat(value);
            default -> value;
        };
        return (T) castedValue;
    }
}
