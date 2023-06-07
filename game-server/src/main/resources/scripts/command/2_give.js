var AbstractCommand = Java.type("com.jftse.emulator.server.core.command.AbstractCommand");
var CommandAdapter = Java.extend(AbstractCommand);

var impl = new CommandAdapter({
    getRank: function () {
        return 1;
    },
    getCommandName: function () {
        return "give";
    },
    getDescription: function () {
        return "Gives item, exp, gold or ap to a player";
    },

    execute: function (connection, params) {
        let currentPlayerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(connection.getClient());

        if (params.length < 1) {
            currentPlayerScriptable.sendChat("Command", "Use -give <ap|gold|exp|item>");
            return;
        }

        let giveType = params[0];
        if (giveType === "ap" || giveType === "gold" || giveType === "exp") {
            if (params.length < 3) {
                currentPlayerScriptable.sendChat("Command", "Use -give <ap|gold|exp> <amount> <player>");
                return;
            }
            let playerName = params[2];
            let player = serviceManager.getPlayerService().findByName(playerName);
            if (player === null || player === undefined) {
                currentPlayerScriptable.sendChat("Command", "Player not found");
                return;
            }

            let playerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(player.getId());
            if (giveType === "ap") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                playerScriptable.giveAp(amount);
                currentPlayerScriptable.sendChat("Command", "You gave " + amount + " AP to " + playerName);
            }
            if (giveType === "gold") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                playerScriptable.giveGold(amount);
                currentPlayerScriptable.sendChat("Command", "You gave " + amount + " gold to " + playerName);
            }
            if (giveType === "exp") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                playerScriptable.giveExp(amount);
                currentPlayerScriptable.sendChat("Command", "You gave " + amount + " exp to " + playerName);
            }
        }
        if (giveType === "item") {
            if (params.length < 4) {
                currentPlayerScriptable.sendChat("Command", "Use -give item <productIndex> <amount> <player>");
                currentPlayerScriptable.sendChat("Command", "Use -give item <itemIndex> <category> <amount> <player>");
                return;
            }
            if (params.length === 4) {
                let productIndex = parseInt(params[1]);
                let amount = parseInt(params[2]);
                let playerName = params[3];
                let player = serviceManager.getPlayerService().findByName(playerName);
                if (player === null || player === undefined) {
                    currentPlayerScriptable.sendChat("Command", "Player not found");
                    return;
                }

                let playerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(player.getId());
                if (isNaN(productIndex) || isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "productIndex and amount must be numbers");
                    return;
                }
                playerScriptable.giveItem(productIndex, amount);
                currentPlayerScriptable.sendChat("Command", "You gave " + amount + " items to " + playerName);
            }
            if (params.length === 5) {
                let itemIndex = parseInt(params[1]);
                let category = params[2];
                let amount = parseInt(params[3]);
                let playerName = params[4];
                let player = serviceManager.getPlayerService().findByName(playerName);
                if (player === null || player === undefined) {
                    currentPlayerScriptable.sendChat("Command", "Player not found");
                    return;
                }

                let playerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(player.getId());
                if (isNaN(itemIndex) || isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "itemIndex and amount must be numbers");
                    return;
                }
                playerScriptable.giveItem(itemIndex, category, amount);
                currentPlayerScriptable.sendChat("Command", "You gave " + amount + " items to " + playerName);
            }
        }
    }
});
