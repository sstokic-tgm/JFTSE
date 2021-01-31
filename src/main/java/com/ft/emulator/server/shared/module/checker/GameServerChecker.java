package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.common.discord.DiscordWebhook;
import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class GameServerChecker extends ServerChecker implements Runnable {
    private final Server server;

    public GameServerChecker(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        boolean isAlive = this.isAlive("0.0.0.0", 5895);
        log.info("game server " + (isAlive ? "is" : "is not") + " online.");

        if (!isAlive) {
            DiscordWebhook discordWebhook = new DiscordWebhook(""); // empty till global config table created
            discordWebhook.setContent("Game Server is down. Trying to restart...");

            try {
                discordWebhook.execute();
                log.info("Trying to restart the game server...");

                server.dispose();
                server.restart();
                server.bind(5895);
            }
            catch (IOException ioe) {
                discordWebhook.setContent("Failed to start Game Server!");
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    log.error("DiscordWebhook error: " ,e);
                }

                log.error("Failed to start game server!", ioe);
            }
            server.start("game server");

            log.info("Game server has been restarted.");

            discordWebhook.setContent("Game Server has been restarted.");
            try {
                discordWebhook.execute();
            } catch (IOException e) {
                log.error("DiscordWebhook error: " ,e);
            }
        }
    }
}