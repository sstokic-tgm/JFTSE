package com.jftse.emulator.server.core.listener;

import com.jftse.emulator.server.core.handler.authentication.BasicAuthHandler;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AuthenticationServerNetworkListener implements ConnectionListener {
    public void cleanUp() {
        // empty..
    }

    public void connected(Connection connection) {
        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        new BasicAuthHandler().sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {
        new BasicAuthHandler().handleDisconnected(connection);
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        switch ("" + exception.getMessage()) {
            case "Connection is closed.":
            case "Connection reset by peer":
            case "Broken pipe":
                break;
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
}
