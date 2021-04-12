package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.common.utilities.ResourceUtil;
import lombok.Getter;
import lombok.Setter;
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
public class AntiCheatHandler {
    private Map<Client, Map<String, Boolean>> clientList;
    private List<String> fileList;

    @PostConstruct
    public void init() {
        clientList = new HashMap<>();
        fileList = getFiles();
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while (reader.ready())
                result.add(reader.readLine());
        } catch (IOException ioe) {
            // empty
        }
        return result;
    }
}