# Validation

Checked with Java 25.0.2, Gradle 9.5.1, Loom 1.17.21, Fabric Loader 0.19.5, Fabric API 0.161.0+26.2, and the official Minecraft 26.2 development classes.

- Gradle `build`: passed, including Java compilation and JAR assembly.
- Fabric Loader bootstrap: passed. All four mixin target classes were transformed and linked; the camera, keyboard input, entity look, and Minecraft interaction hooks were present.
- Configuration edge cases: non-finite speed/range, invalid color, and an entity cap below the minimum were handled correctly.
- Mouse-look bounds: yaw wrapping and upper/lower pitch clamping passed.
- JAR metadata: client-only environment, the pinned Minecraft version, all registered mixin classes, and keybind translations were verified.
- Source review: the mod defines no custom network messages or camera/player-position packet code.

The bootstrap check exits before opening a graphics window. Gameplay, visual output, multiplayer behavior, other mod combinations, and server anti-cheat behavior have **not** been tested. Compilation and class-loading checks do not establish undetectability.

The workspace disallows Unix domain sockets. An optional Loom socket-capability probe was adjusted in the temporary local build cache so the build could run here. That adjustment is not part of the delivered project or JAR; the project uses the upstream Loom dependency.
