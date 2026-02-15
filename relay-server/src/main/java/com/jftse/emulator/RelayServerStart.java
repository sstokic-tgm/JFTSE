package com.jftse.emulator;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.ConnectionInitializer;
import com.jftse.server.core.ServerLoop;
import com.jftse.server.core.StartupBanner;
import com.jftse.server.core.YamlPropertySourceFactory;
import com.jftse.server.core.protocol.PacketAutoRegister;
import com.jftse.server.core.shared.ServerConfService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SpringBootApplication
@EntityScan(basePackages = "com.jftse.entities")
@EnableJpaRepositories(basePackages = "com.jftse.entities")
@ComponentScan(basePackages = "com.jftse")
@PropertySource(value = "classpath:build-info.yml", factory = YamlPropertySourceFactory.class)
@Log4j2
public class RelayServerStart implements CommandLineRunner {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private ServerLoop serverLoop;

    @Autowired
    private RelayManager relayManager;
    @Autowired
    private ServerConfService serverConfService;

    public static void main(String[] args) {
        SpringApplication.run(RelayServerStart.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        PacketAutoRegister.registerAll();

        if (!serverConfService.loadConf(false)) {
            log.error("Failed to load server configuration. Exiting...");
            System.exit(1);
        }

        final boolean useEpoll = Epoll.isAvailable();
        bossGroup = useEpoll ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        workerGroup = useEpoll ? new EpollEventLoopGroup(2) : new NioEventLoopGroup(2);

        Class<? extends ServerChannel> serverChannelClass = useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(serverChannelClass)
                .option(ChannelOption.SO_BACKLOG, 300)
                .childHandler(new ConnectionInitializer())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, 16384)
                .childOption(ChannelOption.SO_SNDBUF, 16384);

        final int port = serverConfService.get("ServerPort", Integer.class);
        b.bind(port).addListener(cf -> {
            if (cf.isSuccess()) {
                serverLoop.start();

                StartupBanner.print(log, "relay-server", port, useEpoll);
            } else {
                log.error("Failed to start relay-server: {}", cf.cause().getMessage(), cf.cause());
            }
        });
    }

    @PreDestroy
    public void onExit() {
        log.info("Shutting down server...");
        relayManager.onExit();
        serverLoop.stop();
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
