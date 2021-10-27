package com.jftse.emulator;

import com.jftse.emulator.common.GlobalSettings;
import com.jftse.emulator.common.discord.DiscordWebhook;
import com.jftse.emulator.server.game.core.anticheat.AntiCheatHeartBeatNetworkListener;
import com.jftse.emulator.server.game.core.game.GameServerNetworkListener;
import com.jftse.emulator.server.game.core.listener.RelayServerNetworkListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.io.IOException;

@SpringBootApplication
@Log4j2
public class StartApplication {
    @Autowired
    private GameServerNetworkListener gameServerNetworkListener;
    @Autowired
    private RelayServerNetworkListener relayServerNetworkListener;
    @Autowired
    private AntiCheatHeartBeatNetworkListener antiCheatHeartBeatNetworkListener;

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
        log.info("Emulator successfully started!");

        DiscordWebhook discordWebhook = new DiscordWebhook(""); // empty till global config table created
        discordWebhook.setContent("Login Server is online.\nGame Server is online.\nMatchmaking Server is online.");
        try {
            discordWebhook.execute();
        } catch (IOException e) {
            log.error("DiscordWebhook error: " ,e);
        }
    }

    @PreDestroy
    public void onExit() {
        log.info("Stopping the emulator...");

        gameServerNetworkListener.cleanUp();
        relayServerNetworkListener.cleanUp();
        if (GlobalSettings.IsAntiCheatEnabled) {
            antiCheatHeartBeatNetworkListener.cleanUp();
        }
    }
}
