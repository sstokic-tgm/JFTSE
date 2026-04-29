include("core/types");

(function (root) {
    root.Commands = root.Commands || {};

    const CommandAdapter = Java.extend(root.Types.AbstractCommand);

    root.Commands.create = function (rank, name, description, executeFn) {
        return new CommandAdapter({
            getRank: function () {
                return rank;
            },

            getCommandName: function () {
                return name;
            },

            getDescription: function () {
                return description;
            },

            execute: executeFn
        });
    };

})(globalThis.Game = globalThis.Game || {});