package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.ServerType;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.net.TCPHandlerV2;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.CMSGDisconnectRequest;
import com.jftse.server.core.shared.packets.CMSGHeartbeat;
import com.jftse.server.core.shared.packets.SMSGDisconnectResponse;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandlerV2<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey) {
        super(ftConnectionAttributeKey);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();

        PacketRegistry.register(CMSGDisconnectRequest.PACKET_ID, this::handleDisconnectRequest);
        //PacketRegistry.register(CMSGHeartbeat.PACKET_ID, this::handleHeartBeat);
    }

    @Override
    public void connected(FTConnection connection) {
        AuthenticationManager.getInstance().queueConnection(connection);
    }

    @Override
    protected void packetReceived(FTConnection connection, IPacket packet) {
        if (packet instanceof CMSGHeartbeat p) {
            handleHeartBeat(connection, p);
        } else {
            connection.queuePacket(packet);
        }
    }

    private void handleHeartBeat(FTConnection connection, CMSGHeartbeat packet) {
        FTClient client = connection.getClient();
        if (client.getAccountId() != null && client.getAccountStatus() == AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
            connection.wantsToCloseConnection();
        }
    }

    private void handleDisconnectRequest(FTConnection connection, CMSGDisconnectRequest packet) {
        SMSGDisconnectResponse response = SMSGDisconnectResponse.builder().status((byte) 0).build();
        connection.sendTCP(response);
        connection.wantsToCloseConnection();
    }

    @Override
    public void disconnected(FTConnection connection) {
        final FTClient client = connection.getClient();
        Long accountId = client.getAccountId();
        if (accountId != null) {
            UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                    .setAccountId(accountId)
                    .setTimestamp(System.currentTimeMillis())
                    .setServer(ServerType.AUTH_SERVER.getValue())
                    .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.DISCONNECT.getValue()).build())
                    .build();
            AuthenticationManager.getInstance().addUpdateAccountRequest(request);
        }
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        FTClient client = connection.getClient();
        Long accountId = client.getAccountId();
        if (accountId != null) {
            log.error("({}) exceptionCaught: {}", accountId, cause.getMessage(), cause);
        }
    }
}
