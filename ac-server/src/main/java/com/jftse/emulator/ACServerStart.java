package com.jftse.emulator;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.net.ConnectionInitializer;
import com.jftse.server.core.handler.PacketHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ACServerStart {
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    @Autowired
    private ConfigService configService;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ACServerStart.class, args);

        PacketHandlerFactory packetHandlerFactory = PacketHandlerFactory.initFactory(log);
        packetHandlerFactory.autoRegister();

        bossGroup = new NioEventLoopGroup(4);
        workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 300)
                    .childHandler(new ConnectionInitializer(packetHandlerFactory))
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_RCVBUF, 16384)
                    .childOption(ChannelOption.SO_SNDBUF, 16384);

            ChannelFuture f = b.bind(ConfigService.getInstance().getValue("anticheat.port", 1337)).sync();
            if (f.isSuccess()) {
                log.info(
                        "\n\n***********************************\n" +
                        "* ac-server successfully started! *\n" +
                        "***********************************");
            }
            f.channel().closeFuture().sync();
        } finally {
            log.info("Stopping server");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void onExit() {
        log.info("Server exited");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
