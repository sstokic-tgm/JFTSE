package com.jftse.emulator.server.shared.module.checker;

import com.jftse.emulator.common.discord.DiscordWebhook;
import com.jftse.emulator.server.networking.Server;
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
            DiscordWebhook discordWebhook = new DiscordWebhook(""); // empty till global config table created
            discordWebhook.setContent("Login Server is down. Trying to restart...");

            try {
                discordWebhook.execute();
                log.info("Trying to restart the authentication server...");

                server.dispose();
                while (server.getServerChannel() != null) { Thread.sleep(250); }

                server.restart();
                server.bind(5894);
            }
            catch (IOException ioe) {
                discordWebhook.setContent("Failed to start Login Server!");
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    log.error("DiscordWebhook error: " ,e);
                }

                log.error("Failed to start authentication server!", ioe);
            } catch (InterruptedException e) {
                log.error("Error while waiting to close the auth server: ", e);
            }
            server.start("auth server");

            log.info("Authentication server has been restarted.");

            discordWebhook.setContent("Login Server has been restarted.");
            try {
                discordWebhook.execute();
            } catch (IOException e) {
                log.error("DiscordWebhook error: " ,e);
            }
        }
    }
}