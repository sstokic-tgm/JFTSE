package com.jftse.emulator.common.service;

import com.jftse.emulator.common.model.config.Config;
import com.jftse.emulator.common.repository.ConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@Log4j2
public class ConfigService {
    private static ConfigService instance;

    @Autowired
    private ConfigRepository configRepository;

    @PostConstruct
    public void init() {
        instance = this;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ConfigService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name, T defaultValue) {
        Optional<Config> optionalConfig = configRepository.findConfigByName(name);
        if (optionalConfig.isEmpty()) {
            return defaultValue;
        }
        Config config = optionalConfig.get();

        String type = config.getType();
        var result = switch (type) {
            case "string" -> (T) config.getValue();
            case "int" -> (T) Integer.valueOf(config.getValue());
            case "double" -> (T) Double.valueOf(config.getValue());
            case "boolean" -> config.getValue().equals("true") || config.getValue().equals("1");
            default -> 0;
        };

        return (T) result;
    }
}
