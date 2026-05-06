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
    public void init() {
        loadConf(true);
    }

    public boolean loadConf(boolean reload) {
        if (!reload && loaded) {
            return true;
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
            loaded = true;
            log.info("Configuration loaded from {}", confFile);
        } catch (Exception e) {
            log.error("Failed to read configuration {}", confFile, e);
            loaded = false;
        }

        return loaded;
    }

    private Path getExecDir() {
        final String confPath = System.getProperty("conf.path");
        if (!StringUtils.isEmpty(confPath)) {
            return Paths.get(confPath).toAbsolutePath().normalize();
        } else {
            return Path.of(".").toAbsolutePath().normalize();
        }
    }

    private String getString(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Configuration key not found: " + key);
        }

        return value;
    }

    public <T> T get(String key, Class<T> type) {
        String value = getString(key);

        Object castedValue;
        if (type == Integer.class) {
            castedValue = Integer.parseInt(value);
        } else if (type == Long.class) {
            castedValue = Long.parseLong(value);
        } else if (type == Boolean.class) {
            castedValue = Boolean.parseBoolean(value);
        } else if (type == Float.class) {
            castedValue = Float.parseFloat(value);
        } else if (type == Double.class) {
            castedValue = Double.parseDouble(value);
        } else {
            castedValue = value;
        }

        return type.cast(castedValue);
    }
}
