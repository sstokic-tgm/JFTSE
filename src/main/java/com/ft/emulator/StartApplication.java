package com.ft.emulator;

import com.ft.emulator.server.game.core.auth.AuthenticationServerNetworkListener;
import com.ft.emulator.server.game.core.game.GameServerNetworkListener;
import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Scanner;

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
            authenticationServer.stop();
            ctx.close();
            System.exit(1);
        }

        gameServer.start("game server");

        log.info("Successfully initialized!");
        log.info("--------------------------------------");

        log.info("Emulator successfully started!");
        log.info("Write exit and confirm with enter to stop the emulator!");

        Scanner scan = new Scanner(System.in);
        String input;
        while (true) {
            input = scan.next();

            if (input.equals("exit"))
                break;
        }

        log.info("Stopping the emulator...");

        authenticationServer.stop();
        gameServer.stop();
        ctx.close();
    }
}
