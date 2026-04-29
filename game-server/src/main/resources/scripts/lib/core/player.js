include("core/types");

(function (root) {
    root.Player = root.Player || {};

    root.Player.of = function (clientOrPlayerId) {
        return new root.Types.PlayerScriptableImpl(clientOrPlayerId);
    }

})(globalThis.Game = globalThis.Game || {});