package com.ft.emulator;

import com.ft.emulator.common.discord.DiscordWebhook;
import com.ft.emulator.server.game.core.auth.AuthenticationServerNetworkListener;
import com.ft.emulator.server.game.core.game.GameServerNetworkListener;
import com.ft.emulator.server.game.core.game.RelayServerNetworkListener;
import com.ft.emulator.server.networking.Server;
import com.ft.emulator.server.shared.module.checker.AuthServerChecker;
import com.ft.emulator.server.shared.module.checker.GameServerChecker;
import com.ft.emulator.server.shared.module.checker.RelayServerChecker;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Log4j2
public class StartApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(StartApplication.class, args);

        log.info("Initializing authentication server...");
        AuthenticationServerNetworkListener authenticationNetworkListener = new AuthenticationServerNetworkListener();
        // post dependency injection for this class
        ctx.getBeanFactory().autowireBean(authenticationNetworkListener);

        Server authenticationServer = new Server();
        authenticationServer.addListener(authenticationNetworkListener);

        try {
            authenticationServer.bind(5894);
        }
        catch (IOException ioe) {
            log.error("Failed to start authentication server!");
            ioe.printStackTrace();
            ctx.close();
            System.exit(1);
        }
        authenticationServer.start("auth server");

        log.info("Successfully initialized!");
        log.info("--------------------------------------");

        log.info("Initializing game server...");
        GameServerNetworkListener gameServerNetworkListener = new GameServerNetworkListener();
        // post dependency injection for this class
        ctx.getBeanFactory().autowireBean(gameServerNetworkListener);

        Server gameServer = new Server();
        gameServer.addListener(gameServerNetworkListener);

        try {
            gameServer.bind(5895);
        }
        catch (IOException ioe) {
            log.error("Failed to start game server!");
            ioe.printStackTrace();
            ctx.close();
            System.exit(1);
        }
        gameServer.start("game server");

        log.info("Successfully initialized!");
        log.info("--------------------------------------");

        log.info("Initializing relay server...");
        RelayServerNetworkListener relayServerNetworkListener = new RelayServerNetworkListener();
        // post dependency injection for this class
        ctx.getBeanFactory().autowireBean(relayServerNetworkListener);

        Server relayServer = new Server();
        relayServer.addListener(relayServerNetworkListener);

        try {
            relayServer.bind(5896);
        }
        catch (IOException ioe) {
            log.error("Failed to start relay server!");
            ioe.printStackTrace();
            ctx.close();
            System.exit(1);
        }
        relayServer.start("relay server");

        log.info("Successfully initialized!");
        log.info("--------------------------------------");

        log.info("Emulator successfully started!");
        log.info("Write exit and confirm with enter to stop the emulator!");

        DiscordWebhook discordWebhook = new DiscordWebhook(""); // empty till global config table created
        discordWebhook.setContent("Login Server is online.\nGame Server is online.\nMatchmaking Server is online.");
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            log.error("DiscordWebhook error: " ,e);
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        executor.scheduleWithFixedDelay(new AuthServerChecker(authenticationServer), 0, 5, TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(new GameServerChecker(gameServer, relayServer), 0, 6, TimeUnit.MINUTES);
        executor.scheduleWithFixedDelay(new RelayServerChecker(relayServer), 0, 7, TimeUnit.MINUTES);

        Scanner scan = new Scanner(System.in);
        String input;
        while (true) {
            input = scan.next();

            if (input.equals("exit"))
                break;
        }

        log.info("Stopping the emulator...");

        executor.shutdown();

        try {
            authenticationServer.dispose();
            gameServer.dispose();
            relayServer.dispose();
        }
        catch (IOException ioe) {
            log.error(ioe.getMessage());
            ctx.close();
            System.exit(1);
        }
        ctx.close();
        System.exit(1);
    }
}
