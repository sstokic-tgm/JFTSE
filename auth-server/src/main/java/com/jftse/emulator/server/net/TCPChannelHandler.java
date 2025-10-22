package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.net.TCPHandlerV2;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.shared.packets.CMSGDisconnectRequest;
import com.jftse.server.core.shared.packets.CMSGHeartbeat;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandlerV2<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey) {
        super(ftConnectionAttributeKey);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
    }

    @Override
    public void connected(FTConnection connection) {
        AuthenticationManager.getInstance().queueConnection(connection);
    }

    @Override
    protected void packetReceived(FTConnection connection, IPacket packet) {
        if (packet instanceof CMSGHeartbeat || packet instanceof CMSGDisconnectRequest) {
            callPacketHandler(connection, packet);
        } else {
            connection.queuePacket(packet);
        }
    }

    private void callPacketHandler(FTConnection connection, IPacket packet) {
        try {
            PacketHandler<FTConnection, IPacket> handler = PacketRegistry.getHandler(packet.getPacketId());
            if (handler != null) {
                handler.handle(connection, packet);
            } else {
                log.warn("No handler for packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId());
            }
        } catch (Exception e) {
            log.error("Error processing packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId(), e);
        }
    }

    @Override
    public void disconnected(FTConnection connection) {
        final FTClient client = connection.getClient();
        if (client != null) {
            final Account account = client.getAccount();
            if (account != null) {
                UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                        .setAccountId(account.getId())
                        .setTimestamp(System.currentTimeMillis())
                        .setServer(ServerType.AUTH_SERVER.getValue())
                        .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.DISCONNECT.getValue()).build())
                        .build();
                AuthenticationManager.getInstance().addUpdateAccountRequest(request);
            }
        }
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
                log.error("({}) exceptionCaught: {}", remoteAddress, cause.getMessage(), cause);
            }
        }
    }
}
