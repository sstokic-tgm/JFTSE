package com.jftse.emulator.server.shared.module.checker;

import com.jftse.emulator.common.discord.DiscordWebhook;
import com.jftse.emulator.server.game.core.game.GameServerNetworkListener;
import com.jftse.emulator.server.game.core.game.RelayServerNetworkListener;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.networking.Server;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class GameServerChecker extends ServerChecker implements Runnable {
    private final Server gameServer;
    private final Server relayServer;
    private final GameServerNetworkListener gameServerNetworkListener;
    private final RelayServerNetworkListener relayServerNetworkListener;

    public GameServerChecker(Server gameServer, Server relayServer, ConnectionListener... connectionListener) {
        this.gameServer = gameServer;
        this.relayServer = relayServer;
        this.gameServerNetworkListener = (GameServerNetworkListener) connectionListener[0];
        this.relayServerNetworkListener = (RelayServerNetworkListener) connectionListener[1];
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
                gameServerNetworkListener.cleanUp();
                while (gameServer.getServerChannel() != null) { Thread.sleep(250); }

                gameServer.restart();
                gameServer.bind(5895);
            }
            catch (IOException ioe) {
                discordWebhook.setContent("Failed to start Game Server!");
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    log.error("DiscordWebhook error: " , e);
                }

                log.error("Failed to start game server!", ioe);
            } catch (InterruptedException e) {
                log.error("Error while waiting to close the game server: ", e);
            }

            discordWebhook.setContent("Shutting down Matchmaking Server. Trying to restart...");
            try {
                discordWebhook.execute();
                log.info("Trying to restart the relay server...");

                relayServer.dispose();
                relayServerNetworkListener.cleanUp();
                while (relayServer.getServerChannel() != null) { Thread.sleep(250); }

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
            } catch (InterruptedException e) {
                log.error("Error while waiting to close the relay server: ", e);
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