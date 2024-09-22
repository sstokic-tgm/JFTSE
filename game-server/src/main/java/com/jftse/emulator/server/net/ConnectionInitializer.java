package com.jftse.emulator.server.net;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.codec.PacketDecoder;
import com.jftse.server.core.codec.PacketEncoder;
import com.jftse.server.core.handler.PacketHandlerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger packetLogger = LogManager.getLogger("PacketLogger");

    private final AttributeKey<FTConnection> FT_CONNECTION_ATTRIBUTE_KEY;
    private final TCPChannelHandler tcpChannelHandler;
    private final EventExecutorGroup group = new DefaultEventExecutorGroup(8);

    private final boolean encryptionEnabled;

    public ConnectionInitializer(final PacketHandlerFactory packetHandlerFactory) {
        FT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("connection");
        this.tcpChannelHandler = new TCPChannelHandler(FT_CONNECTION_ATTRIBUTE_KEY, packetHandlerFactory);
        this.encryptionEnabled = ConfigService.getInstance().getValue("network.encryption.enabled", false);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final int decryptionKey = encryptionEnabled ? getRandomBigInteger().intValueExact() : 0;
        final int encryptionKey = encryptionEnabled ? getRandomBigInteger().intValueExact() : 0;

        FTConnection connection = new FTConnection(decryptionKey, encryptionKey, ServerType.GAME_SERVER);
        ch.attr(FT_CONNECTION_ATTRIBUTE_KEY).set(connection);

        ch.pipeline().addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
        //ch.pipeline().addLast(new FlushConsolidationHandler());
        ch.pipeline().addLast("decoder", new PacketDecoder(decryptionKey, packetLogger));
        ch.pipeline().addLast("encoder", new PacketEncoder(encryptionKey, packetLogger));
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
