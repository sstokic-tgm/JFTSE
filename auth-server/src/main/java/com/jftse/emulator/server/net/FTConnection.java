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

    private ConcurrentLinkedDeque<Packet[]> packetSendQueue;

    public FTConnection(ChannelHandlerContext chx, final int decryptionKey, final int encryptionKey) {
        super(chx, decryptionKey, encryptionKey);

        packetSendQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void sendTCP(Packet... packets) {
        if (packets == null || packets.length == 0)
            throw new IllegalArgumentException("Packet cannot be null.");

        log.debug("packetSendQueue size = " + packetSendQueue.size());
        packetSendQueue.add(packets);
        log.debug("newly added item, packetSendQueue size = " + packetSendQueue.size());
        if (packetSendQueue.peek() != null) {
            final int queueSize = packetSendQueue.size();
            log.debug("queueSize = " + queueSize);
            for (int i = 0; i < queueSize; i++) {
                Packet[] packetArray = packetSendQueue.poll();
                if (packetArray != null) {
                    log.debug("packetArray size = " + packetArray.length);
                    for (Packet packet : packetArray) {
                        chx.write(packet);
                        log.debug("wrote " + packet.toString());
                    }
                    chx.flush();
                }
            }
        }
    }
}
