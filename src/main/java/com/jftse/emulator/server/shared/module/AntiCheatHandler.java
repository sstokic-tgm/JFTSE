package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.ResourceUtil;
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

@Log4j2
@Service
@Getter
@Setter
public class AntiCheatHandler {
    private Map<Client, Map<String, Boolean>> clientList;
    private List<String> fileList;

    @Autowired
    private ConfigService configService;

    @PostConstruct
    public void init() {
        clientList = new HashMap<>();
        if (configService.getValue("anticheat.enabled", false))
            fileList = getFiles();
        else
            fileList = new ArrayList<>();
    }

    public void addClient(Client client) {
        Map<String, Boolean> fileData = new HashMap<>();
        fileList.forEach(f -> fileData.put(f, false));
        clientList.put(client, fileData);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public Map<String, Boolean> getFilesByClient(Client client) { return this.clientList.get(client); }

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