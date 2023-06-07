var AbstractCommand = Java.type("com.jftse.emulator.server.core.command.AbstractCommand");
var CommandAdapter = Java.extend(AbstractCommand);

var impl = new CommandAdapter({
    getRank: function () {
        return 1;
    },
    getCommandName: function () {
        return "get";
    },
    getDescription: function () {
        return "Gives item, exp, gold or ap to current player";
    },

    execute: function (connection, params) {
        let currentPlayerScriptable = new (Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"))(connection.getClient());

        if (params.length < 1) {
            currentPlayerScriptable.sendChat("Command", "Use -get <ap|gold|exp|item>");
            return;
        }

        let giveType = params[0];
        if (giveType === "ap" || giveType === "gold" || giveType === "exp") {
            if (params.length < 2) {
                currentPlayerScriptable.sendChat("Command", "Use -get <ap|gold|exp> <amount>");
                return;
            }
            if (giveType === "ap") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                currentPlayerScriptable.giveAp(amount);
                currentPlayerScriptable.sendChat("Command", "You have been given " + amount + " AP");
            }
            if (giveType === "gold") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                currentPlayerScriptable.giveGold(amount);
                currentPlayerScriptable.sendChat("Command", "You have been given " + amount + " gold");
            }
            if (giveType === "exp") {
                let amount = parseInt(params[1]);
                if (isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "amount must be a number");
                    return;
                }
                currentPlayerScriptable.giveExp(amount);
                currentPlayerScriptable.sendChat("Command", "You have been given " + amount + " exp");
            }
        }
        if (giveType === "item") {
            if (params.length < 3) {
                currentPlayerScriptable.sendChat("Command", "Use -get item <productIndex> <amount>");
                currentPlayerScriptable.sendChat("Command", "Use -get item <itemIndex> <category> <amount>");
                return;
            }
            if (params.length === 3) {
                let productIndex = parseInt(params[1]);
                let amount = parseInt(params[2]);

                if (isNaN(productIndex) || isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "productIndex and amount must be numbers");
                    return;
                }

                currentPlayerScriptable.giveItem(productIndex, amount);
                currentPlayerScriptable.sendChat("Command", productIndex + " has been given to you " + amount + " times");
            }
            if (params.length === 4) {
                let itemIndex = parseInt(params[1]);
                let category = params[2];
                let amount = parseInt(params[3]);

                if (isNaN(itemIndex) || isNaN(amount)) {
                    currentPlayerScriptable.sendChat("Command", "itemIndex and amount must be numbers");
                    return;
                }

                currentPlayerScriptable.giveItem(itemIndex, category, amount);
                currentPlayerScriptable.sendChat("Command", itemIndex + " has been given to you " + amount + " times");
            }
        }
    }
});
