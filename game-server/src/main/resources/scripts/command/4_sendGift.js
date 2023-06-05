var AbstractCommand = Java.type("com.jftse.emulator.server.core.command.AbstractCommand");
var CommandAdapter = Java.extend(AbstractCommand);

var impl = new CommandAdapter({
    getRank: function () {
        return 1;
    },
    getCommandName: function () {
        return "sendGift";
    },
    getDescription: function () {
        return "Sends gift to a player";
    },

    execute: function (connection, params) {
        let currentPlayerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(connection.getClient());

        if (params.length < 4) {
            currentPlayerScriptable.sendChat("Command", "Usage: -sendGift <playerName> <productIndex> <amount> <message>");
            return;
        }
        let playerName = params[0];
        let productIndex = parseInt(params[1]);
        let amount = parseInt(params[2]);
        let message = params[3];

        if (isNaN(productIndex) || isNaN(amount)) {
            currentPlayerScriptable.sendChat("Command", "productIndex and amount must be numbers");
            return;
        }

        let player = serviceManager.getPlayerService().findByName(playerName);
        if (player === null || player === undefined) {
            currentPlayerScriptable.sendChat("Command", "Player not found");
            return;
        }

        let playerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(player.getId());

        playerScriptable.sendGift(productIndex, amount, message);
        currentPlayerScriptable.sendChat("Command", "You sent gift to " + playerName);
    }
});
