package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.server.core.net.TCPHandlerV2;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.service.BlockedIPService;
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

        this.blockedIPService = RelayManager.getInstance().getBlockedIPService();

        PacketRegistry.register(CMSGDisconnectRequest.PACKET_ID, this::handleDisconnectRequest);
        //PacketRegistry.register(CMSGHeartbeat.PACKET_ID, this::handleHeartBeat);
    }

    @Override
    public void connected(FTConnection connection) {
        RelayManager.getInstance().queueConnection(connection);
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
        // empty
    }

    private void handleDisconnectRequest(FTConnection connection, CMSGDisconnectRequest packet) {
        SMSGDisconnectResponse response = SMSGDisconnectResponse.builder().status((byte) 0).build();
        connection.sendTCP(response);
        connection.wantsToCloseConnection();
    }

    @Override
    public void disconnected(FTConnection connection) {
        FTClient client = connection.getClient();
        client.getGameSessionId().ifPresent(sessionId -> RelayManager.getInstance().removeClient(sessionId, client));
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        // empty
    }
}
