package com.jftse.emulator.server.net;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.server.core.net.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@Log4j2
public class FTClient extends Client<FTConnection> {
    private ConcurrentHashMap<String, Boolean> fileList;

    private final AtomicBoolean isClosingConnection;

    public FTClient() {
        fileList = new ConcurrentHashMap<>();
        isClosingConnection = new AtomicBoolean(false);

        if (ConfigService.getInstance().getValue("anticheat.enabled", false)) {
            List<String> files = getFiles();
            files.forEach(f -> fileList.put(f, false));
        }
    }

    private List<String> getFiles() {
        List<String> result = new ArrayList<>();
        InputStream is = ResourceUtil.getResource("res-files.txt");
        if (is == null) {
            log.info("Anti-Cheat is enabled but no res-files.txt found!");
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while (reader.ready())
                result.add(reader.readLine());
        } catch (IOException ioe) {
            // empty
        }
        return result;
    }
}
