(function (root) {
    root.Types = root.Types || {};

    root.Types.AbstractCommand = Java.type(
        "com.jftse.emulator.server.core.command.AbstractCommand"
    );

    root.Types.PlayerScriptableImpl = Java.type(
        "com.jftse.emulator.server.core.interaction.PlayerScriptableImpl"
    );

    root.Types.SMSGChatMessageRoom = Java.type(
        "com.jftse.server.core.shared.packets.chat.SMSGChatMessageRoom"
    );

})(globalThis.Game = globalThis.Game || {});