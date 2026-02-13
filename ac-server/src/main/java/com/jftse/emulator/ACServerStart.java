package com.jftse.emulator;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.manager.ACManager;
import com.jftse.emulator.server.net.ConnectionInitializer;
import com.jftse.server.core.protocol.PacketAutoRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SpringBootApplication
@EntityScan(basePackages = "com.jftse.entities")
@EnableJpaRepositories(basePackages = "com.jftse.entities")
@ComponentScan(basePackages = "com.jftse")
@Log4j2
public class ACServerStart implements CommandLineRunner {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ACManager acManager;

    public static void main(String[] args) {
        SpringApplication.run(ACServerStart.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        PacketAutoRegister.registerAll();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(2);

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 300)
                .childHandler(new ConnectionInitializer())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 16384)
                .childOption(ChannelOption.SO_SNDBUF, 16384);

        b.bind(configService.getValue("anticheat.port", 1337)).addListener(cf -> {
            if (cf.isSuccess()) {
                log.info("""
                        
                        *************************************
                        * ac-server successfully started! *
                        *************************************""");
            } else {
                log.error("Failed to start ac-server: {}", cf.cause().getMessage(), cf.cause());
            }
        });
    }

    @PreDestroy
    public void onExit() {
        log.info("Shutting down server...");
        acManager.onExit();
        if (workerGroup != null && bossGroup != null) {
            Future<?> workerGroupFuture = workerGroup.shutdownGracefully();
            Future<?> bossGroupFuture = bossGroup.shutdownGracefully();
            try {
                workerGroupFuture.get();
                bossGroupFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while exiting server: {}", e.getMessage(), e);
            }
        }
    }
}
