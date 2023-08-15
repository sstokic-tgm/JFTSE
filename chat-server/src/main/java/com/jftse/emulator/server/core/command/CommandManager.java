package com.jftse.emulator.server.core.command;

import com.jftse.emulator.common.scripting.ScriptFile;
import com.jftse.emulator.common.scripting.ScriptManager;
import com.jftse.emulator.server.core.command.commands.gm.*;
import com.jftse.emulator.server.core.command.commands.player.*;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.log.CommandLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.ScriptContext;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Getter
@Setter
@Log4j2
public class CommandManager {
    private static CommandManager instance;

    private static final char COMMAND_HEADING = '-';

    private HashMap<String, Command> registeredCommands;

    @PostConstruct
    public void init() {
        instance = this;

        registeredCommands = new LinkedHashMap<>();

        log.info("Loading commands...");
        registerCommands();
        registerScriptFileCommands();
        log.info("Commands has been loaded.");

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static CommandManager getInstance() {
        return instance;
    }

    public boolean isCommand(String content) {
        char heading = content.charAt(0);
        return heading == COMMAND_HEADING && registeredCommands.get(getCommandArgumentList(content).get(0).substring(1)) != null;
    }

    public void handle(FTConnection connection, String content) {
        if (connection.getClient() != null) {
            try {
                executeCommand(connection, content);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private List<String> getCommandArgumentList(String content) {
        List<String> commandArgumentList = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(content);
        while (m.find())
            commandArgumentList.add(m.group(1).replace("\"", ""));

        return commandArgumentList;
    }

    private void executeCommand(FTConnection connection, String content) {
        List<String> commandArgumentList = getCommandArgumentList(content);

        String commandName = commandArgumentList.get(0);
        commandName = commandName.substring(1);

        commandArgumentList.remove(0);

        final AbstractCommand command = (AbstractCommand) registeredCommands.get(commandName);
        if (command == null) {
            if (connection.getClient().getActiveRoom() != null) {
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Command '" + commandName + "' is not available");
                connection.sendTCP(chatRoomAnswerPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Command '" + commandName + "' is not available");
                connection.sendTCP(chatLobbyAnswerPacket);
            }
            return;
        }

        Account account = connection.getClient().getAccount();
        if (account == null)
            return;

        if (!account.getGameMaster() && command.getRank() > 0) {
            if (connection.getClient().getActiveRoom() != null) {
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "You do not have permission to use this command");
                connection.sendTCP(chatRoomAnswerPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Command", "You do not have permission to use this command");
                connection.sendTCP(chatLobbyAnswerPacket);
            }
            return;
        }
        logCommand(connection.getClient(), command, commandArgumentList);
        command.execute(connection, commandArgumentList);
    }

    public void registerCommand(String commandName, int rank, AbstractCommand command) {
        command.setRank(rank);
        command.setCommandName(commandName);
        registeredCommands.put(commandName, command);
        log.info("Registered command -" + commandName);
    }

    private void registerCommands() {
        registerCommand("og", 0, new OpenGachaCommand());
        registerCommand("ban", 1, new BanPlayerCommand());
        registerCommand("unban", 1, new UnbanPlayerCommand());
        registerCommand("sN", 1, new ServerNoticeCommand());
        registerCommand("serverKick", 1, new ServerKickCommand());
        registerCommand("rsLogin", 1, new ResetLoginStatusCommand());
        registerCommand("reloadScripts", 1, new ReloadScriptsCommand());
        registerCommand("event", 1, new EventCommand());
    }

    private void registerScriptFileCommands() {
        Optional<ScriptManager> scriptManager = GameManager.getInstance().getScriptManager();
        if (scriptManager.isPresent()) {
            ScriptManager sm = scriptManager.get();
            List<ScriptFile> scriptFiles = sm.getScriptFiles("COMMAND");
            for (ScriptFile scriptFile : scriptFiles) {
                try {
                    AbstractCommand command = getAbstractCommandObj(scriptFile, sm);
                    registerCommand(command.getCommandName(), command.getRank(), command);
                } catch (Exception e) {
                    log.error("Error on register command from script: " + scriptFile.getFile().getName().split("_")[1].split("\\.")[0] + ". ScriptException: " + e.getMessage());
                }
            }
        }
    }

    private void logCommand(FTClient client, AbstractCommand command, List<String> commandArgumentList) {
        StringBuilder sb = new StringBuilder();
        commandArgumentList.forEach(s -> sb.append(s).append(" "));

        CommandLog commandLog = new CommandLog();
        commandLog.setCommand(command.getCommandName());
        commandLog.setArguments(sb.toString());
        commandLog.setPlayer(client.getPlayer());
        GameManager.getInstance().getServiceManager().getCommandLogService().save(commandLog);
    }

    public AbstractCommand getAbstractCommandObj(ScriptFile scriptFile, ScriptManager sm) throws Exception {
        Bindings bindings = sm.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("gameManager", GameManager.getInstance());
        bindings.put("serviceManager", GameManager.getInstance().getServiceManager());
        bindings.put("commandManager", this);
        sm.eval(scriptFile, bindings);

        return (AbstractCommand) sm.getScriptEngine().get("impl");
    }
}
