// === Example Event 2 ===
// This script demonstrates two ways to register listeners for the ON_LOGIN event.
//
// The first listener uses a scoped function inside an IIFE (Immediately Invoked Function Expression).
// The second listener uses a global function.
// Both listeners receive an FTClient object as their first parameter (see: com.jftse.emulator.server.net).
// The script is loaded and compiled once on server startup or reload.
//
// Globally available for scripts:
// gameManager: instance of GameManager
// serviceManager: instance of ServiceManager
// geb: GameEventBus instance for registering listeners
//
// Supported events: com.jftse.emulator.server.core.life.event.GameEventType

// -- Scoped function inside IIFE --
// Does NOT leak function names into global scope.
// Recommended for avoiding function name conflicts with other scripts.

// (function () {
//     function onLogin(client) {
//         console.log("Hello World " + client.getPlayer().getName() + " from Example Event 2 scoped function");
//     }
//
//     geb.on("ON_LOGIN", onLogin);
// })();

// -- Global function --
// This function is defined in the global scope.
// Avoid naming collisions by using unique names or preferring scoped functions.

// function onLoginGlobal(client) {
//     console.log("Hello World " + client.getPlayer().getName() + " from Example Event 2 global function");
// }
//
// geb.on("ON_LOGIN", onLoginGlobal);
