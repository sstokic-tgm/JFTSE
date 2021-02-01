package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.common.discord.DiscordWebhook;
import com.ft.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class GameServerChecker extends ServerChecker implements Runnable {
    private final Server gameServer;
    private final Server relayServer;

    public GameServerChecker(Server gameServer, Server relayServer) {
        this.gameServer = gameServer;
        this.relayServer = relayServer;
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

                gameServer.dispose();
                gameServer.restart();
                gameServer.bind(5895);
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

            discordWebhook.setContent("Shutting down Matchmaking Server. Trying to restart...");
            try {
                discordWebhook.execute();
                log.info("Trying to restart the relay server...");

                relayServer.dispose();
                relayServer.restart();
                relayServer.bind(5896);
            }
            catch (IOException ioe) {
                discordWebhook.setContent("Failed to start Matchmaking Server!");
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    log.error("DiscordWebhook error: " ,e);
                }

                log.error("Failed to start relay server!", ioe);
            }

            gameServer.start("game server");
            relayServer.start("relay server");

            log.info("Game server has been restarted.");
            log.info("Relay server has been restarted.");

            discordWebhook.setContent("Game Server has been restarted.\nMatchmaking Server has been restarted.");
            try {
                discordWebhook.execute();
            } catch (IOException e) {
                log.error("DiscordWebhook error: " ,e);
            }
        }
    }
}