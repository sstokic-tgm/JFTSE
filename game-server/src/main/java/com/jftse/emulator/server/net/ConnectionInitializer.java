package com.jftse.emulator.server.net;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.entities.database.model.log.ServerType;
import com.jftse.server.core.codec.PacketDecoder;
import com.jftse.server.core.codec.PacketEncoder;
import com.jftse.server.core.handler.PacketHandlerFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final AttributeKey<FTConnection> FT_CONNECTION_ATTRIBUTE_KEY;
    private final TCPChannelHandler tcpChannelHandler;

    public ConnectionInitializer(final PacketHandlerFactory packetHandlerFactory) {
        FT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("connection");
        this.tcpChannelHandler = new TCPChannelHandler(FT_CONNECTION_ATTRIBUTE_KEY, packetHandlerFactory);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final int decryptionKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;
        final int encryptionKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;

        FTConnection connection = new FTConnection(decryptionKey, encryptionKey);
        connection.setServerType(ServerType.GAME_SERVER);

        ch.attr(FT_CONNECTION_ATTRIBUTE_KEY).set(connection);

        ch.pipeline().addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
        ch.pipeline().addLast(new FlushConsolidationHandler());
        ch.pipeline().addLast("decoder", new PacketDecoder(decryptionKey, log));
        ch.pipeline().addLast("encoder", new PacketEncoder(encryptionKey, log));
        ch.pipeline().addLast(tcpChannelHandler);
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
