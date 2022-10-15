package com.jftse.emulator.server.net;

import com.jftse.server.core.net.Connection;
import com.jftse.server.core.protocol.Packet;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
@Log4j2
public class FTConnection extends Connection<FTClient> {
    private String hwid;
    private FTClient client;

    private long lastReadTime = 0L;

    public FTConnection(ChannelHandlerContext chx, final int decryptionKey, final int encryptionKey) {
        super(chx, decryptionKey, encryptionKey);
    }

    @Override
    public void sendTCP(Packet... packets) {
        if (packets == null || packets.length == 0)
            throw new IllegalArgumentException("Packet cannot be null.");

        for (Packet packet : packets) {
            chx.write(packet);
        }
        chx.flush();
    }
}
