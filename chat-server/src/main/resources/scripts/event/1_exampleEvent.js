// === Example Event 1 ===
// This script registers a listener for the ON_LOGIN event.
//
// This is a simple example of how to use the Game Event Bus (geb) to listen for events in the game server.
// This is a simple example using an anonymous function.
// The listener is passed a FTClient object (see: com.jftse.emulator.server.net).
// The script is loaded and compiled once on server startup or reload.
// No global function names are created (safe from collisions with other scripts).
//
// Globally available for scripts:
// gameManager: instance of GameManager
// serviceManager: instance of ServiceManager
// geb: GameEventBus instance for registering listeners
//
// Supported events: com.jftse.emulator.server.core.life.event.GameEventType

// geb.on("ON_LOGIN", function(client) {
//     console.log("Hello World " + client.getPlayer().getName() + " from Example Event 1");
// });
