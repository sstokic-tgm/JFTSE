package com.jftse.emulator.server.core.command;

import com.jftse.emulator.server.core.command.commands.gm.*;
import com.jftse.emulator.server.core.command.commands.player.*;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.entities.database.model.account.Account;
import com.jftse.emulator.server.networking.Connection;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        registeredCommands = new HashMap<>();

        registerCommands();

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static CommandManager getInstance() {
        return instance;
    }

    public boolean isCommand(String content) {
        char heading = content.charAt(0);
        return heading == COMMAND_HEADING && registeredCommands.get(getCommandArgumentList(content).get(0).substring(1)) != null;
    }

    public void handle(Connection connection, String content) {
        if (connection.getClient() != null) {
            try {
                handleInternal(connection, content);
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

    private void handleInternal(Connection connection, String content) {
        List<String> commandArgumentList = getCommandArgumentList(content);

        String commandName = commandArgumentList.get(0);
        commandName = commandName.substring(1);

        commandArgumentList.remove(0);

        final Command command = registeredCommands.get(commandName);
        if (command == null) {
            if (connection.getClient().getActiveRoom() != null) {
                S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Command '" + commandName + "' is not available");
                if (connection.isConnected())
                    connection.sendTCP(chatRoomAnswerPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Command '" + commandName + "' is not available");
                if (connection.isConnected())
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
                if (connection.isConnected())
                    connection.sendTCP(chatRoomAnswerPacket);
            } else {
                S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket((char) 0, "Command", "You do not have permission to use this command");
                if (connection.isConnected())
                    connection.sendTCP(chatLobbyAnswerPacket);
            }
            return;
        }
        command.execute(connection, commandArgumentList);
    }

    private void addCommand(String commandName, int rank, Class<? extends Command> commandClass) {
        if (registeredCommands.containsKey(commandName)) {
            log.error("Error on register command with name: " + commandName + ". Already exists.");
            return;
        }

        try {
            Command commandInstance = commandClass.getDeclaredConstructor().newInstance();
            commandInstance.setRank(rank);

            registeredCommands.put(commandName, commandInstance);
            log.info("Registered command -" + commandName);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void registerCommands() {
        log.info("Loading commands...");
        addCommand("og", 0, OpenGachaCommand.class);
        addCommand("hard", 0, HardModeCommand.class);
        addCommand("arcade", 0, ArcadeModeCommand.class);
        addCommand("random", 0, RandomModeCommand.class);
        addCommand("latency", 0, LatencyCommand.class);
        addCommand("pointback", 0, PointbackCommand.class);
        addCommand("ban", 1, BanPlayerCommand.class);
        addCommand("unban", 1, UnbanPlayerCommand.class);
        addCommand("sN", 1, ServerNoticeCommand.class);
        addCommand("serverKick", 1, ServerKickCommand.class);
        addCommand("rsLogin", 1, ResetLoginStatusCommand.class);
        log.info("Commands has been loaded.");
    }
}
