package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Getter
@Setter
@Log4j2
public class AntiCheatManager {
    private static AntiCheatManager instance;

    @Getter
    public class AntiCheatClient {
        private final Client client;
        private final Map<String, Boolean> fileList;

        public AntiCheatClient(Client client, Map<String, Boolean> fileList) {
            this.client = client;
            this.fileList = fileList;
        }
    }

    private ArrayList<AntiCheatClient> clients;
    private List<String> fileList;

    @Autowired
    private ConfigService configService;

    @PostConstruct
    public void init() {
        instance = this;

        clients = new ArrayList<>();

        if (configService.getValue("anticheat.enabled", false))
            fileList = getFiles();
        else
            fileList = new ArrayList<>();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static AntiCheatManager getInstance() {
        return instance;
    }

    public void addClient(Client client) {
        Map<String, Boolean> fileData = new HashMap<>();
        fileList.forEach(f -> fileData.put(f, false));

        AntiCheatClient antiCheatClient = new AntiCheatClient(client, fileData);
        clients.add(antiCheatClient);
    }

    public void removeClient(Client client) {
        clients.removeIf(acc -> acc.getClient().equals(client));
    }

    public Map<String, Boolean> getFilesByClient(Client client) {
        Map<String, Boolean> result = null;

        AntiCheatClient antiCheatClient = clients.stream().filter(x -> x.client.equals(client)).findFirst().orElse(null);
        if (antiCheatClient != null) {
            result = antiCheatClient.getFileList();
        }

        return result;
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
