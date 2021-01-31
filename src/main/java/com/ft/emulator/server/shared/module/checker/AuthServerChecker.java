package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class AuthServerChecker extends ServerChecker implements Runnable {
    private final Server server;

    public AuthServerChecker(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        boolean isAlive = this.isAlive("0.0.0.0", 5894);
        log.info("auth server " + (isAlive ? "is" : "is not") + " online.");

        if (!isAlive) {
            log.info("Trying to restart the authentication server...");

            try {
                server.dispose();
                server.restart();
                server.bind(5894);
            }
            catch (IOException ioe) {
                log.error("Failed to start authentication server!", ioe);
            }
            server.start("auth server");

            log.info("Authentication server has been restarted.");
        }
    }
}