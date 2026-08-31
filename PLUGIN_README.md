# Writing a JFTSE plugin

This describes how to add a new **native (Java) plugin module** to JFTSE, following the pattern
established by `tower-mode`. If you just need to hook a single command/event/boss-phase and don't
need a new DB table or match-long state, you probably don't need a plugin at all — see
[Do you even need a plugin?](#do-you-even-need-a-plugin) first.

The full design rationale (why this shape, what was considered and rejected) lives in
[`plugins/tower-mode/PLUGIN_ARCHITECTURE_PLAN.md`](plugins/tower-mode/PLUGIN_ARCHITECTURE_PLAN.md) —
read it once if anything below feels arbitrary. This document is the shorter "just tell me what to
do" version.

--------------

* [Do you even need a plugin?](#do-you-even-need-a-plugin)
* [How plugins work, in one paragraph](#how-plugins-work-in-one-paragraph)
* [The one hard rule: package names](#the-one-hard-rule-package-names)
* [Step 1 — module skeleton](#step-1--module-skeleton)
* [Step 2 — wire it into the root build](#step-2--wire-it-into-the-root-build)
* [Step 3 — hook into core behavior](#step-3--hook-into-core-behavior)
* [Step 4 — a new command](#step-4--a-new-command)
* [Step 5 — a new DB table](#step-5--a-new-db-table)
* [Step 6 — build, deploy, run](#step-6--build-deploy-run)
* [Sanity-check your plugin actually loaded](#sanity-check-your-plugin-actually-loaded)
* [Limits / non-goals](#limits--non-goals)

## Do you even need a plugin?

JFTSE already has a scripting layer (`ScriptManagerV2` + `GameEventBus` + script commands +
`PhaseScript`, all under `game-server/src/main/resources/scripts/`) that lets you add a command,
react to an event, or override a single boss phase's combat methods **without writing any Java or
rebuilding anything** — drop a `.js` file in the right folder.

Use a script if your feature needs none of the following. Use a native plugin (this document) if it
needs **any** of them:

- a new JPA entity / DB table
- state that lives for the whole match (not just a script's own closure)
- a decision at the level of the match's own lifecycle (which guardians spawn, whether the match
  times out, etc.) rather than a single event moment

Tower Mode needs all three, which is why it's a plugin and not a script.

## How plugins work, in one paragraph

A plugin is its own Maven module under `plugins/<name>/`, built separately from `game-server` and
**not** bundled into it. `game-server` has zero compile-time or runtime dependency on any plugin —
it only knows about a handful of small extension-point interfaces (`GuardianBattleStateProvider`,
`MatchplayLifecycleExtension`, `WaveCompletionExtension`, `MatchTimerExtension`) that live in
`game-server` itself. Your plugin depends on `game-server` (one-directional), implements whichever
of those interfaces it needs as `@Component`-annotated Spring beans, and gets deployed by dropping
its built jar into a folder (`plugins-lib`) that's added to `game-server`'s classpath **at startup**
via Spring Boot's `PropertiesLauncher` (`-Dloader.path=plugins-lib`). Spring's own component/entity
scanning then just... finds your classes, because they're on the classpath and under the right
package prefix (see next section). No plugin registry, no manual wiring — running `game-server`
with an empty `plugins-lib` folder behaves exactly like the plugin doesn't exist.

The whole `plugins/` directory is `.gitignore`'d — plugins are never part of the public repo. The
root `pom.xml` only adds a plugin to the Maven reactor build if its `pom.xml` is actually present on
disk (file-activated profile), so a normal public clone builds fine without ever knowing plugins
exist.

## The one hard rule: package names

`game-server`'s `@ComponentScan`/`@EntityScan`/`@EnableJpaRepositories` (see
`GameServerStart.java`) scan a fixed base package — they are **not** wildcarded to also cover
arbitrary plugin packages. This means:

**Every class in your plugin that needs Spring to find it (`@Component`, `@Service`, `@Entity`,
repository interfaces, ...) must live under the same package prefixes `game-server` already scans**
— in practice `com.jftse.emulator...` for beans and `com.jftse.entities...` for JPA
entities/repositories, mirroring `tower-mode`'s own layout:

```
plugins/tower-mode/src/main/java/com/jftse/emulator/server/core/matchplay/guardian/TowerModeLifecycleExtension.java
plugins/tower-mode/src/main/java/com/jftse/emulator/server/core/command/commands/player/TowerModeCommandRegistrar.java
plugins/tower-mode/src/main/java/com/jftse/entities/database/model/battle/TowerModeFloorGuardians.java
plugins/tower-mode/src/main/java/com/jftse/entities/database/repository/battle/TowerModeFloorGuardiansRepository.java
```

If you instead put your classes under something like `com.jftse.plugin.myplugin`, Spring will
silently never see them — no error, they just won't be picked up. Pick class/file names that won't
collide with existing ones in these packages (there's no module-level namespacing to save you).

## Step 1 — module skeleton

```
plugins/<name>/
  pom.xml
  sql/                         (optional — see Step 5)
  src/main/java/com/jftse/...  (see package rule above)
```

`pom.xml`, copy from `plugins/tower-mode/pom.xml` and adjust `<artifactId>`/`<name>`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <parent>
        <artifactId>emulator-parent</artifactId>
        <groupId>com.jftse</groupId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>my-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>my-plugin</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <!-- classifier "classes" - game-server's default artifact is the runnable fat jar
                 (BOOT-INF/classes nested, unusable as a compile dependency). This classifier picks
                 the plain jar built specifically for this purpose. See the maven-jar-plugin
                 execution in game-server/pom.xml. -->
            <groupId>com.jftse</groupId>
            <artifactId>game-server</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <classifier>classes</classifier>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
        </dependency>
        <!-- Only needed if Spring Boot's BOM would otherwise pick an older managed version for a
             transitive dependency you need pinned - keep in sync with game-server's own pin if so. -->
    </dependencies>
</project>
```

## Step 2 — wire it into the root build

Add a file-activated profile to the root `pom.xml`, next to `with-tower-mode` — copy its shape:

```xml
<profile>
    <id>with-my-plugin</id>
    <activation>
        <file>
            <exists>plugins/my-plugin/pom.xml</exists>
        </file>
    </activation>
    <modules>
        <module>plugins/my-plugin</module>
    </modules>
</profile>
```

No `-P` flag needed — it activates automatically whenever `plugins/my-plugin/pom.xml` exists on
disk, and is simply absent from the Maven reactor otherwise. **Do not** add your plugin as a
`<dependency>` of `game-server/pom.xml` — that direction is intentionally never allowed (it's what
lets `game-server` stay pluginless-by-default and lets multiple plugins coexist without knowing
about each other).

## Step 3 — hook into core behavior

Four extension points exist today, all under
`game-server/src/main/java/com/jftse/emulator/server/core/matchplay/extension/`. Implement whichever
you need as a `@Component` in your plugin module — `ServiceManager` autowires
`List<TheInterface>` for each of these (`@Autowired(required = false)`, defaulting to an empty
list), so zero plugins installed behaves identically to today with no `if`-branch anywhere in core
code.

| Interface | Called from | Purpose |
|---|---|---|
| `GuardianBattleStateProvider` | `MatchplayGuardianGame#createGuardianBattleState` | Return a custom `GuardianBattleState` for a guardian, or `Optional.empty()` to defer to the default hard-mode/normal calculation |
| `MatchplayLifecycleExtension` | `MatchplayGuardianModeHandler#onPrepare`/`onStart` | `overrideInitialGuardians(...)` (return `null` to defer) and `onMatchStarting(...)` (notify-only) |
| `WaveCompletionExtension` | `SpellHitsTargetHandler#handleAllGuardiansDead` | `handlesWaveCompletion(game)` — return `true` to take over "all guardians dead" handling instead of the default boss-transition logic |
| `MatchTimerExtension` | `DefeatTimerTask` | `overridesTimer(game)` — return `true` to take over (or suppress) the match timer instead of the default map-based `playTime`/`bossPlayTime` |

Every one of these has a `room.isModeActive(MODE_ID)` (or equivalent) check as the first line of the
implementation, so your extension only actually does anything for matches your plugin cares about —
see `TowerModeLifecycleExtension` for the reference shape:

```java
@Component
public class MyPluginLifecycleExtension implements MatchplayLifecycleExtension {
    @Override
    public List<GuardianBase> overrideInitialGuardians(MatchplayGuardianGame game, Room room) {
        if (!room.isModeActive("my-mode")) {
            return null; // not mine, fall back to default logic
        }
        // ... your logic
    }
}
```

**Marking a room as running your mode:** `Room.setModeActive(String modeId, boolean active)` /
`Room.isModeActive(String modeId)` take any string — you don't need to add a constant to core code
for a new mode id, just pick a unique string in your own plugin and keep the constant there.

Note that `HardModeCommand`/`RandomModeCommand` treat *any* active extension mode as exclusive with
Hard/Random mode (`!room.getActiveExtensionModes().isEmpty()`) — so switching a room into your mode
already blocks Hard/Random there, with no core change needed on your part.

**Per-match state:** don't add fields to `MatchplayGuardianGame`. Use its generic state bag instead:

```java
public class MyPluginGameState {
    // your fields here

    public static MyPluginGameState of(MatchplayGuardianGame game) {
        return game.getExtensionState(MyPluginGameState.class, MyPluginGameState::new);
    }
}
```

If what you need doesn't fit any of the four interfaces above, that's a sign either core needs a new
extension point (discuss before adding — these are a stable-ish SPI, changing a signature breaks
every plugin using it) or your feature actually belongs in the scripting layer instead.

## Step 4 — a new command

Don't add a line to `CommandManager.registerCommands()`. Register it yourself from a `@Component` in
your own module — `CommandManager#registerCommand` is already public for exactly this:

```java
@Component
public class MyPluginCommandRegistrar {
    @Autowired
    public MyPluginCommandRegistrar(CommandManager commandManager) {
        commandManager.registerCommand("mycommand", 0, new MyPluginCommand());
    }
}
```

## Step 5 — a new DB table

1. Define the `@Entity` and its Spring Data repository under `com.jftse.entities.database.model...`
   / `com.jftse.entities.database.repository...` (see [package rule](#the-one-hard-rule-package-names)).
2. Define a `@Service`, injected normally via `@Autowired` from other beans in your own module.
   If some class in your plugin isn't Spring-managed and needs static access to your services (as
   `TowerMode.java` does), mirror `TowerModeServiceRegistry`'s `getInstance()` pattern — don't add
   your services to `game-server`'s `ServiceManager` (it can't depend on your module).
3. Hibernate (`ddl-auto=update`) creates the table automatically the first time `game-server` boots
   with your plugin jar on the classpath, but column order ends up alphabetical. If you need a
   specific column order, write your own `create_*.sql`/`insert_*.sql` under `plugins/<name>/sql/`
   and extend `scripts/import_sql.sh` the same way Tower's tables are wired in — guarded by a
   file-existence check so a public clone (no `plugins/` folder) just warns and skips, unaffected.

## Step 6 — build, deploy, run

```
cd <repo root>
mvn clean install
```

Your plugin's profile activates automatically (file-based, see Step 2) and builds alongside
everything else. Output: `plugins/<name>/target/<name>-1.0.0-SNAPSHOT.jar`.

Deploying it is a manual copy — Maven does **not** do this for you:

```
cp plugins/<name>/target/<name>-1.0.0-SNAPSHOT.jar game-server/plugins-lib/
```

Then run `game-server` with `loader.path` pointing at that folder. **The path is resolved relative
to the JVM's working directory, not the jar's location** — get this wrong and `loader.path` fails
silently (see [next section](#sanity-check-your-plugin-actually-loaded)), so match it to however you
actually launch:

```
# cwd = game-server/target (the jar's own folder, plugins-lib is a direct sibling)
java -Dloader.path=plugins-lib -jar game-server.jar

# cwd = game-server/target/dist - the working directory server.conf/scripts/logs actually need
# (see below), two levels below plugins-lib
java -Dloader.path=../../plugins-lib -jar ../game-server.jar
```

(`LOADER_PATH=plugins-lib` as an env var instead of `-Dloader.path=` works the same way, same
relative-path rule.) Multiple plugin jars can sit in the same `plugins-lib` folder at once;
`loader.path` also accepts a comma-separated list of paths if you'd rather keep them elsewhere.

**Working directory gotcha (unrelated to plugins specifically, but you'll hit it in the same
breath):** `server.conf` and the `scripts/` folder are copied by the build to
`game-server/target/dist/`, not to `game-server/target/` itself — so `java -jar game-server.jar` run
straight from `target/` fails on startup (`Configuration key not found: ...`) even with no plugin
involved. Run from `target/dist/` instead, referencing the jar one level up, as in the second example
above.

## Sanity-check your plugin actually loaded

`loader.path` fails silently if the path or jar name is wrong — no error, `game-server` just starts
without your plugin. Give your plugin's main registrar/service a startup log line (see
`TowerModeServiceRegistry#init`, which logs `"TowerModeServiceRegistry initialized"` from a
`@PostConstruct` method) and grep the boot log for it. If it's missing:

- double-check the jar is actually in the folder `loader.path` points to
- double-check your `@Component`/`@Entity` classes are under the scanned package prefixes
  ([package rule](#the-one-hard-rule-package-names)) — this is the most common mistake
- confirm the jar was built against the current `game-server-classes.jar` (stale plugin jar built
  against an older core is a common source of silent `NoSuchMethodError`/`ClassNotFoundException` at
  runtime, since there's no compile-time check tying them together at deploy time)

## Limits / non-goals

- **No hot-reload / isolated classloading.** Adding or replacing a plugin jar requires a
  `game-server` restart; there's a single shared classpath and a single Spring
  `ApplicationContext` — a misbehaving plugin can affect the whole process.
- **No license/signature verification** — anything in `plugins-lib` is trusted code.
- **The SPI (the 4 interfaces in Step 3) is shared across every plugin.** Changing a method
  signature on one of them breaks every plugin implementing it, not just yours — coordinate before
  changing them, don't just add a parameter.
