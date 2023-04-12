package com.jftse.emulator.server.net;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.codec.PacketDecoder;
import com.jftse.server.core.codec.PacketEncoder;
import com.jftse.server.core.handler.PacketHandlerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.math.BigInteger;
import java.util.Random;

@Log4j2
public class ConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final AttributeKey<FTConnection> FT_CONNECTION_ATTRIBUTE_KEY;
    private final TCPChannelHandler tcpChannelHandler;
    private final EventExecutorGroup group = new UnorderedThreadPoolEventExecutor(8);

    public ConnectionInitializer(final PacketHandlerFactory packetHandlerFactory) {
        FT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("connection");
        this.tcpChannelHandler = new TCPChannelHandler(FT_CONNECTION_ATTRIBUTE_KEY, packetHandlerFactory);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final int decryptionKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;
        final int encryptionKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;

        FTConnection connection = new FTConnection(decryptionKey, encryptionKey);
        connection.setServerType(ServerType.AC_SERVER);

        ch.attr(FT_CONNECTION_ATTRIBUTE_KEY).set(connection);

        ch.pipeline().addLast(new FlushConsolidationHandler());
        ch.pipeline().addLast("decoder", new PacketDecoder(decryptionKey, log));
        ch.pipeline().addLast("encoder", new PacketEncoder(encryptionKey, log));
        ch.pipeline().addLast(group, tcpChannelHandler);
    }

    private BigInteger getRandomBigInteger() {
        Random rnd = new Random();
        BigInteger upperLimit = new BigInteger("10000");
        BigInteger result;
        do {
            result = new BigInteger(upperLimit.bitLength(), rnd);
        } while (result.compareTo(upperLimit) > 0);
        return result;
    }
}
