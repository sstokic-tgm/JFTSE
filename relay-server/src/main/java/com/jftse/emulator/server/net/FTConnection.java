package com.jftse.emulator.server.net;

import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.JoinedPacket;
import com.jftse.server.core.protocol.Packet;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class FTConnection extends Connection<FTClient> {
    private String hwid;

    private long lastReadTime = 0L;

    public FTConnection(final int decryptionKey, final int encryptionKey) {
        super(decryptionKey, encryptionKey);
    }

    @Override
    public ChannelFuture sendTCP(Packet... packets) {
        if (packets == null || packets.length == 0)
            throw new IllegalArgumentException("Packet cannot be null.");

        JoinedPacket joinedPackets = new JoinedPacket(packets);
        return ctx.writeAndFlush(joinedPackets);
    }
}
