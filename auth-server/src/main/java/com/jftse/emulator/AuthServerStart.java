package com.jftse.emulator;

import com.jftse.emulator.server.net.ConnectionInitializer;
import com.jftse.server.core.handler.PacketHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PreDestroy;

@SpringBootApplication
@EntityScan(basePackages = "com.jftse.entities")
@EnableJpaRepositories(basePackages = "com.jftse.entities")
@ComponentScan(basePackages = "com.jftse")
@Log4j2
public class AuthServerStart {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(AuthServerStart.class, args);

        PacketHandlerFactory packetHandlerFactory = PacketHandlerFactory.initFactory();
        packetHandlerFactory.autoRegister();



        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 300)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_TIMEOUT, 12000)
                    .childHandler(new ConnectionInitializer(packetHandlerFactory))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 16384)
                    .childOption(ChannelOption.SO_SNDBUF, 16384);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(5894).sync();// (8)
            if(f.isSuccess()) {
                log.info(
                        "\n*************************************\n" +
                        "* auth-server successfully started! *\n" +
                        "*************************************");
            }
            f.channel().closeFuture().sync(); // (10)
        } finally {
            log.info("Stopping server");
            workerGroup.shutdownGracefully();// (11)
            bossGroup.shutdownGracefully();// (12)
        }
    }

    @PreDestroy
    public void onExit() {
        log.info("Server is closing...");
    }
}
