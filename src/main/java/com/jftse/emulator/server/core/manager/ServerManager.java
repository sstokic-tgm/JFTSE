package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.packet.packets.S2CServerNoticePacket;
import com.jftse.emulator.server.networking.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Getter
@Setter
@Log4j2
public class ServerManager {
    private static ServerManager instance;

    private List<Server> serverList;

    private AtomicBoolean serverNoticeIsSet;
    private long serverNoticeTime; // -1 = not set, 0 = infinite, time > 0 = time
    private String serverNoticeMessage;

    @PostConstruct
    public void init() {
        instance = this;

        serverList = new ArrayList<>();

        serverNoticeIsSet = new AtomicBoolean(false);
        serverNoticeTime = -1;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ServerManager getInstance() {
        return instance;
    }

    public synchronized void add(Server server) {
        serverList.add(server);
    }

    public synchronized boolean remove(Server server) {
        return serverList.remove(server);
    }

    public synchronized final int size() {
        return serverList.size();
    }

    public synchronized List<Server> getServerList() {
        return serverList;
    }

    public synchronized Server getServerByName(String name) {
        for (Server server : serverList) {
            if (server.getUpdateThread().getName().equals(name))
                return server;
        }
        return null;
    }

    public synchronized Server getServerByPort(int port) {
        for (Server server : serverList) {
            if (server.getTcpPort() == port)
                return server;
        }
        return null;
    }

    public synchronized List<String> getServerNames() {
        final List<String> serverNames = new ArrayList<>();
        for (Server server : serverList) {
            serverNames.add(server.getUpdateThread().getName());
        }
        return serverNames;
    }

    public synchronized void broadcastServerNotice(String message, long time) {
        if (!serverNoticeIsSet.get()) {
            serverNoticeMessage = message;
            serverNoticeTime = time;

            S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(message);
            final List<Server> serverList = new ArrayList<>(getServerList());
            for (Server server : serverList) {
                server.sendToAllTcp(serverNoticePacket);
            }
            serverNoticeIsSet.set(!StringUtils.isEmpty(message));
        }
    }
}
