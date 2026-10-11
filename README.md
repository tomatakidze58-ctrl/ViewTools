# ViewTools — Fabric 26.2

Source code for a client-only Freecam and entity ESP mod. Requires **Minecraft Java 26.2, Fabric Loader 0.19.5 or newer, Fabric API 0.161.0+26.2 or newer for 26.2, and Java 25**.

An already compiled mod is included at `dist/viewtools-1.0.0+mc26.2.jar`. To try it, install Fabric for 26.2, add Fabric API for 26.2 to your instance's `mods` folder, and copy this JAR there too. The complete source and build tools are included below it.

## Controls

| Control | Action |
| --- | --- |
| F6 | Toggle Freecam |
| F7 | Toggle entity ESP |
| F8 | Open settings |
| Movement keys, normally WASD | Move the camera; forward follows your view |
| Jump, normally Space | Move the camera up |
| Sneak, normally Shift | Move the camera down |
| Sprint, normally Ctrl | Speed boost |
| Mouse | Look around |

Change all three mod keybinds in **Options → Controls → Key Binds → ViewTools**. Camera movement follows your existing Minecraft movement keybinds. Freecam and ESP start disabled and reset when you disconnect.

## Features

- Detached camera with no camera collision, smooth position interpolation, and configurable speed.
- Mouse sensitivity and inversion follow Minecraft's settings.
- The camera does not change your player's position or rotation. Normal gravity, damage, server corrections, and networking continue.
- Keyboard movement input, attacking, block breaking, item use, item drops, and offhand swaps are blocked while Freecam is active. Inventory screens remain available.
- Freecam switches off on damage, death, disconnect, changing world/player, entering a vehicle/bed, or a player displacement over four blocks between client ticks.
- Entity boxes and optional name/distance labels, with a through-walls toggle.
- Separate filters/colors for players, hostile mobs, other living mobs, and dropped items. Your own player is excluded.
- Nearest-first entity cap and configurable ESP range to limit rendering work.
- In-game settings and a small status HUD.
- Vanilla 26.2 gizmo rendering; no raw OpenGL calls or extra rendering libraries.

ESP can display only entities already sent to your client. Freecam can explore only terrain the client has loaded; it does not request distant chunks. Your real player remains vulnerable. **Client-side does not guarantee undetectability or compatibility with server anti-cheat.**

## Build on Windows

1. Install a **Java 25 JDK** from [Adoptium](https://adoptium.net/temurin/releases/?version=25), including the PATH option. Open a new terminal after installation.
2. Extract this ZIP. Open the `ViewTools` folder containing `build.gradle` and `gradlew.bat`.
3. Double-click `build-windows.bat`, or run the command below in that folder:

   ```powershell
   .\gradlew.bat build
   ```

4. Wait for **BUILD SUCCESSFUL**. The first build downloads Gradle, Minecraft's development dependencies, and Fabric.
5. Install `build/libs/viewtools-1.0.0+mc26.2.jar` alongside Fabric API in your instance's `mods` folder. The `-sources.jar` is for developers and is not the mod to install.

Do not put the source ZIP in `mods`. You do not need to install Gradle separately; the official wrapper is included.

On Linux/macOS:

```bash
chmod +x gradlew
./gradlew build
```

For development, open the extracted project in IntelliJ IDEA, set its project and Gradle JVMs to Java 25, and use `gradlew runClient`.

## Build with GitHub Actions

1. Create a GitHub repository and upload the **contents** of the `ViewTools` folder to its root. `build.gradle` must be at the root, not inside another `ViewTools` folder.
2. Include `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `src/`, and `.github/workflows/main.yml`. If the website does not upload `.github`, use **Add file → Create new file**, name it `.github/workflows/main.yml`, and paste the included workflow into it.
3. Open **Actions → Build ViewTools**, then open the latest successful run. You can also use **Run workflow** to start a build manually.
4. Download the **ViewTools-26.2** artifact, extract it, and install the JAR in `mods` with Fabric API for 26.2.

## Configuration

The first launch creates `config/viewtools.json`. Most options are available through F8. For colors, sprint multiplier, the camera distance limit, or the entity cap, close Minecraft, edit that JSON file, and restart. Colors use `#RRGGBB`.

Defaults: camera speed 12 blocks/second, sprint multiplier 3, camera limit 256 blocks from the player's eyes, ESP range 128 blocks from the camera, at most 256 entities, players/hostile mobs on, other living mobs/items off, names on, through-walls on, filled boxes off.

## Source layout

| File | Responsibility |
| --- | --- |
| `ViewToolsClient.java` | Client initialization, controls, tick/disconnect events, status HUD |
| `camera/FreecamController.java` | Camera state, movement, lifecycle, click handling |
| `render/EntityOverlay.java` | Entity selection and vanilla gizmo submission |
| `config/ViewToolsConfig.java` | JSON persistence and validation |
| `ui/SettingsScreen.java` | In-game settings |
| `mixin/CameraMixin.java` | Camera placement before frustum setup |
| `mixin/KeyboardInputMixin.java` | Neutral keyboard input for the player |
| `mixin/EntityLookMixin.java` | Redirect local mouse look to the camera |
| `mixin/MinecraftInteractionMixin.java` | Disable world interactions during Freecam |

Java sources are under `src/client/java/dev/viewtools/`. All mixins are registered only in the client environment. The mod adds no custom network payloads and does not cancel server packets.

## Version-specific references

- [Fabric for Minecraft 26.2](https://fabricmc.net/2026/06/15/262.html)
- [Fabric key mappings](https://docs.fabricmc.net/develop/key-mappings)
- [Fabric rendering documentation](https://docs.fabricmc.net/develop/rendering/world)
- [Minecraft 26.2 migration primer](https://docs.neoforged.net/primer/docs/26.2/)

This project is pinned to 26.2. Changing only the version in `gradle.properties` will not port it to 1.21.11 or another release.

See `VALIDATION.md` for the checks performed and their limits.
