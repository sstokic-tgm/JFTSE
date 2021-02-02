package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.common.discord.DiscordWebhook;
import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class RelayServerChecker extends ServerChecker implements Runnable {
    private final Server server;

    public RelayServerChecker(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        boolean isAlive = this.isAlive("0.0.0.0", 5896);
        log.info("relay server " + (isAlive ? "is" : "is not") + " online.");

        if (!isAlive) {
            DiscordWebhook discordWebhook = new DiscordWebhook(""); // empty till global config table created
            discordWebhook.setContent("Matchmaking Server is down. Trying to restart...");

            try {
                discordWebhook.execute();
                log.info("Trying to restart the relay server...");

                server.dispose();
                while (server.getServerChannel() != null) { Thread.sleep(250); }

                server.restart();
                server.bind(5896);
            }
            catch (IOException ioe) {
                discordWebhook.setContent("Failed to start Matchmaking Server!");
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    log.error("DiscordWebhook error: " ,e);
                }

                log.error("Failed to start relay server!", ioe);
            } catch (InterruptedException e) {
                log.error("Error while waiting to close the relay server: ", e);
            }
            server.start("relay server");

            log.info("Relay server has been restarted.");

            discordWebhook.setContent("Matchmaking Server has been restarted.");
            try {
                discordWebhook.execute();
            } catch (IOException e) {
                log.error("DiscordWebhook error: " ,e);
            }
        }
    }
}