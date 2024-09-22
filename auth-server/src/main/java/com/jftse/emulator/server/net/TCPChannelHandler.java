package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.rpc.client.TransitionServiceImpl;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import com.jftse.server.core.thread.ThreadManager;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandler<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(ftConnectionAttributeKey, phf);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
    }

    @Override
    public void connected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Active");

        FTClient client = new FTClient();

        client.setConnection(connection);
        connection.setClient(client);

        AuthenticationManager.getInstance().addClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    public void handlerNotFound(FTConnection connection, Packet packet) throws Exception {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.warn("(" + remoteAddress + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    public void packetNotProcessed(FTConnection connection, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void disconnected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Inactive");

        final FTClient client = connection.getClient();
        ThreadManager.getInstance().schedule(() -> {
            if (client != null) {
                Account account = client.getAccount();
                if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                    final TransitionServiceImpl transitionService = ServiceManager.getInstance().getTransitionService();

                    transitionService.notifyTransition(ServerType.GAME_SERVER, account.getId())
                            .thenCompose(response -> {
                                if (!response.getSuccess())
                                    return transitionService.notifyTransition(ServerType.CHAT_SERVER, account.getId());
                                else
                                    return CompletableFuture.completedFuture(response);
                            })
                            .thenAccept(response -> {
                                if (!response.getSuccess())
                                    logoutAccount(client, account);
                            })
                            .whenComplete((response, throwable) -> {
                                if (throwable != null)
                                    logoutAccount(client, account);

                                AuthenticationManager.getInstance().removeClient(client);
                            });
                }
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void logoutAccount(FTClient client, Account account) {
        account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
        account.setLoggedInServer(ServerType.NONE);
        client.saveAccount(account);
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        if (!cause.equals(ReadTimeoutException.INSTANCE)) {
            var shouldHandleException = switch (cause.getMessage()) {
                case "Connection reset", "Connection timed out", "No route to host" -> false;
                default -> true;
            };

            if (shouldHandleException) {
                InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
                String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
                log.warn("(" + remoteAddress + ") exceptionCaught: " + cause.getMessage(), cause);
            }
        }
    }
}
