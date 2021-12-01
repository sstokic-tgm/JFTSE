package com.jftse.emulator;

import com.jftse.emulator.common.discord.DiscordWebhook;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.listener.AntiCheatHeartBeatNetworkListener;
import com.jftse.emulator.server.core.listener.GameServerNetworkListener;
import com.jftse.emulator.server.core.listener.RelayServerNetworkListener;
import com.jftse.emulator.server.core.manager.ServerManager;
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

    @Autowired
    private ConfigService configService;

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
        if (configService.getValue("anticheat.enabled", false)) {
            antiCheatHeartBeatNetworkListener.cleanUp();
        }

        ServerManager.getInstance().getServerList().forEach(s -> {
            try {
                s.dispose();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        ServerManager.getInstance().getServerList().clear();
    }
}
