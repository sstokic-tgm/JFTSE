package com.ft.emulator.server.shared.module;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
public class RelayHandler {
    private List<Client> clientList;

    @PostConstruct
    public void init() {
        clientList = new ArrayList<>();
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public void removeClient(int index) {
        clientList.remove(index);
    }
}
