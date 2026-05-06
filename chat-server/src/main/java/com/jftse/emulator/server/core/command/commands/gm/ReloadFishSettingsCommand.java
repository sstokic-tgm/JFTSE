package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.interaction.PlayerScriptableImpl;
import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.net.FTConnection;

import java.util.List;

public class ReloadFishSettingsCommand extends AbstractCommand {
    public ReloadFishSettingsCommand() {
        setDescription("Reloads fish settings");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        PlayerScriptableImpl playerScriptable = new PlayerScriptableImpl(connection.getClient());

        FishManager.getInstance().reloadFishSettings();
        playerScriptable.sendChat("Server", "Fish settings reloaded successfully.");
    }
}
