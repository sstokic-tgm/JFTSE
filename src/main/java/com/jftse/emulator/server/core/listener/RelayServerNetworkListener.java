package com.jftse.emulator.server.core.listener;

import com.jftse.emulator.server.core.handler.relay.BasicRelayHandler;
import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.packet.packets.S2CServerNoticePacket;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class RelayServerNetworkListener implements ConnectionListener {
    @Autowired
    private RelayManager relayManager;
    @Autowired
    private GameSessionManager gameSessionManager;

    @Autowired
    private ServerManager serverManager;

    public void cleanUp() {
        relayManager.getClientList().clear();
        gameSessionManager.getGameSessionList().clear();
    }

    public void connected(Connection connection) {
        long timeout = TimeUnit.MINUTES.toMillis(2);
        connection.getTcpConnection().setTimeoutMillis((int) timeout);

        new BasicRelayHandler().sendWelcomePacket(connection);

        if (serverManager.isServerNoticeIsSet()) {
            S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(serverManager.getServerNoticeMessage());
            connection.sendTCP(serverNoticePacket);
        }
    }

    public void disconnected(Connection connection) {
        new BasicRelayHandler().handleDisconnected(connection);
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
        new BasicRelayHandler().handleTimeout(connection);
    }
}
