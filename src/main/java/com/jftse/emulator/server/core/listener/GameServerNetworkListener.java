package com.jftse.emulator.server.core.listener;

import com.jftse.emulator.server.core.handler.game.BasicGameHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.packet.packets.S2CServerNoticePacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class GameServerNetworkListener implements ConnectionListener {
    @Autowired
    private GameManager gameManager;

    @Autowired
    private ServerManager serverManager;

    public void cleanUp() {
        // reset status
        gameManager.getClients().stream().collect(Collectors.toList()).forEach(client -> {
            Account account = client.getAccount();
            if (account.getStatus() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
                client.saveAccount(account);
            }

            gameManager.getClients().remove(client);
        });

        gameManager.getRooms().clear();
        GameSessionManager.getInstance().getGameSessionList().clear();
    }

    public void connected(Connection connection) {
        long timeout = TimeUnit.SECONDS.toMillis(30);
        connection.getTcpConnection().setTimeoutMillis((int) timeout);
        
        Client client = new Client();
        client.setConnection(connection);

        gameManager.addClient(client);
        connection.setClient(client);

        new BasicGameHandler().sendWelcomePacket(connection);

        if (serverManager.isServerNoticeIsSet()) {
            S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(serverManager.getServerNoticeMessage());
            connection.sendTCP(serverNoticePacket);
        }
    }

    public void disconnected(Connection connection) {
        new BasicGameHandler().handleDisconnected(connection);
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
