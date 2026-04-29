// include("core/player");
include("core/types");
include("core/commands");
include("math/math");

var impl = Game.Commands.create(0, "toss", "Tosses a coin. Usage: -toss", function (connection, params) {
    const client = connection.getClient();
    const player = client.getPlayer(); // session object
    // const scriptable = Game.Player.of(player.getId());
    if (!player) {
        return;
    }


    let message = player.getName() + " is tossing a coin... " + (MathLib.randomInt(0, 1) === 0 ? "Heads!" : "Tails!");

    let chatRoomMessage = Game.Types.SMSGChatMessageRoom.builder()
        .type(2)
        .sender(player.getName())
        .message(message)
        .textColor(client.getTextMode())
        .build();
    gameManager.sendPacketToAllClientsInSameRoom(chatRoomMessage, connection);
});
