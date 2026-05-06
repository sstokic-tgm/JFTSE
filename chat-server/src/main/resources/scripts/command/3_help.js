var AbstractCommand = Java.type("com.jftse.emulator.server.core.command.AbstractCommand");
var CommandAdapter = Java.extend(AbstractCommand);

var impl = new CommandAdapter({
    getRank: function () {
        return 0;
    },
    getCommandName: function () {
        return "help";
    },
    getDescription: function () {
        return "Shows help for commands";
    },

    execute: function (connection, params) {
        let currentPlayerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(connection.getClient());
        let isGameMaster = currentPlayerScriptable.getClient().isGameMaster();
        let commands = commandManager.getRegisteredCommands();

        currentPlayerScriptable.sendChat("Command", "Available commands:");
        for (let [commandName, command] of commands) {
            let rank = command.getRank();
            if (rank > 0 && !isGameMaster) {
                continue;
            }
            currentPlayerScriptable.sendChat("Command", "-" + commandName + " - " + command.getDescription());
        }
    }
});
