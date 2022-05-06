package com.jftse.emulator.server.core.listener;

import com.jftse.emulator.server.core.handler.anticheat.BasicAntiCheatHandler;
import com.jftse.emulator.server.core.handler.authentication.BasicAuthHandler;
import com.jftse.emulator.server.core.manager.AntiCheatManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class AntiCheatHeartBeatNetworkListener implements ConnectionListener {
    @Autowired
    private AntiCheatManager antiCheatManager;

    public void connected(Connection connection) {
        long timeout = TimeUnit.SECONDS.toMillis(30);
        connection.getTcpConnection().setTimeoutMillis((int) timeout);

        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        antiCheatManager.addClient(client);
        ThreadManager.getInstance().schedule(() -> new BasicAuthHandler().sendWelcomePacket(connection), 1, TimeUnit.SECONDS);
    }

    public void disconnected(Connection connection) {
        new BasicAntiCheatHandler().handleDisconnected(connection);
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        switch (exception.getMessage()) {
            case "Socket channel reached EOF.":
                break;
            case "Connection is closed.":
            case "Connection reset by peer":
            case "Broken pipe":
            default:
                String hostAddress;
                if (connection.getRemoteAddressTCP() != null)
                    hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
                else
                    hostAddress = "null";
                log.error(hostAddress + " " + exception.getMessage(), exception);
        }
    }

    public void onTimeout(Connection connection) {
        connection.close();
    }

    public void cleanUp() {
        // empty...
    }
}