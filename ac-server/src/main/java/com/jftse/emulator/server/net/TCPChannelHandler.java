package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.ACManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.net.TCPHandlerV2;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.ClientWhitelistService;
import com.jftse.server.core.shared.packets.SMSGInitHandshake;
import com.jftse.server.core.thread.ThreadManager;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandlerV2<FTConnection> {
    private final ClientWhitelistService clientWhitelistService;
    private final BlockedIPService blockedIPService;
    private final AuthenticationService authenticationService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey) {
        super(ftConnectionAttributeKey);

        this.clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public void connected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";

        FTClient client = new FTClient();

        client.setIp(remoteAddress.substring(1, remoteAddress.lastIndexOf(":")));
        client.setPort(Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1)));
        client.setConnection(connection);
        connection.setClient(client);

        ACManager.getInstance().addClient(client);

        ClientWhitelist clientWhitelist = new ClientWhitelist();
        clientWhitelist.setIp(client.getIp());
        clientWhitelist.setPort(client.getPort());
        clientWhitelist.setFlagged(false);
        clientWhitelist.setIsAuthenticated(false);
        clientWhitelist.setIsActive(true);
        clientWhitelistService.save(clientWhitelist);

        SMSGInitHandshake initHandshakePacket = SMSGInitHandshake.builder()
                .decKey(connection.getDecryptionKey())
                .encKey(connection.getEncryptionKey())
                .decTblIdx(0)
                .encTblIdx(0)
                .build();
        connection.sendTCP(initHandshakePacket);
    }

    @Override
    protected void packetReceived(FTConnection connection, IPacket packet) {
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
        final String hostAddress = client.getIp();
        final String hwid = connection.getHwid();
        ThreadManager.getInstance().schedule(() -> {
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, hwid);
            if (clientWhitelist != null) {
                clientWhitelist.setIsActive(false);
                clientWhitelist = clientWhitelistService.save(clientWhitelist);
            }
            ACManager.getInstance().removeClient(client);
        }, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        FTClient client = connection.getClient();

    }
}
