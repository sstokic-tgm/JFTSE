package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.common.scripting.ScriptManagerFactory;
import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.command.CommandManager;
import com.jftse.emulator.server.core.interaction.PlayerScriptable;
import com.jftse.emulator.server.core.interaction.PlayerScriptableImpl;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTConnection;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Log4j2
public class ReloadScriptsCommand extends AbstractCommand {
    private final GameManager gameManager;
    private final CommandManager commandManager;

    private final String MESSAGE_SUCCESS = "Scripts reloaded";
    private final String MESSAGE_FAIL = "Scripts not reloaded";
    private final String MESSAGE_SENDER = "Server";

    public ReloadScriptsCommand() {
        setDescription("Reloads all scripts");

        this.gameManager = GameManager.getInstance();
        this.commandManager = CommandManager.getInstance();
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        PlayerScriptableImpl playerScriptable = new PlayerScriptableImpl(connection.getClient());

        Optional<ScriptManager> scriptManager = ScriptManagerFactory.loadScripts("scripts", () -> log);
        boolean valid = false;
        if (scriptManager.isPresent()) {
            gameManager.setScriptManager(scriptManager);

            valid = registerScriptFileCommands(playerScriptable);
            if (!valid) {
                playerScriptable.sendChat(MESSAGE_SENDER, MESSAGE_FAIL);
                return;
            }
            valid = registerScriptFileEvents(playerScriptable);
            if (!valid) {
                playerScriptable.sendChat(MESSAGE_SENDER, MESSAGE_FAIL);
                return;
            }
        }

        if (valid) {
            playerScriptable.sendChat(MESSAGE_SENDER, MESSAGE_SUCCESS);
        } else {
            playerScriptable.sendChat(MESSAGE_SENDER, MESSAGE_FAIL);
        }
    }

    private boolean registerScriptFileCommands(PlayerScriptable playerScriptable) {
        Optional<ScriptManager> scriptManager = gameManager.getScriptManager();
        boolean hasRegisteredACommand = false;
        if (scriptManager.isPresent()) {
            ScriptManager sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("COMMAND");
            playerScriptable.sendChat("Server", "Reloading commands...");
            for (ScriptFile scriptFile : scriptFiles) {
                try {
                    AbstractCommand command = commandManager.getAbstractCommandObj(scriptFile, sm);
                    commandManager.registerCommand(command.getCommandName(), command.getRank(), command);
                    hasRegisteredACommand = true;
                } catch (Exception e) {
                    playerScriptable.sendChat("Server", "Error on reload command from script: " + scriptFile.getFile().getName().split("_")[1].split("\\.")[0]);
                    return false;
                }
            }
        }
        return hasRegisteredACommand;
    }

    private boolean registerScriptFileEvents(PlayerScriptable playerScriptable) {
        return true;
    }
}
