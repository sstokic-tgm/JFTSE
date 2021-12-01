package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.core.listener.AuthenticationServerNetworkListener;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.core.manager.ThreadManager;
import com.jftse.emulator.server.networking.Server;
import com.jftse.emulator.server.shared.module.checker.AuthServerChecker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@Order(2)
public class AuthenticationServerStart implements CommandLineRunner {
    @Autowired
    private AuthenticationServerNetworkListener authenticationNetworkListener;

    @Autowired
    private ServerManager serverManager;
    @Autowired
    private ThreadManager threadManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing authentication server...");

        Server authenticationServer = new Server();
        authenticationServer.addListener(authenticationNetworkListener);
        try {
            authenticationServer.bind(5894);
        }
        catch (IOException ioe) {
            log.error("Failed to start authentication server!");
            ioe.printStackTrace();
            System.exit(1);
        }
        authenticationServer.start("auth server");

        log.info("Successfully initialized");
        log.info("--------------------------------------");

        serverManager.add(authenticationServer);

        threadManager.schedule(new AuthServerChecker(authenticationServer), 1, 5, TimeUnit.MINUTES);
    }
}
