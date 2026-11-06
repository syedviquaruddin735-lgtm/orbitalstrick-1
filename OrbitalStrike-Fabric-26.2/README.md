# OrbitalStrike — Fabric 26.2 port

This is a **Fabric server mod project**, not a Paper plugin. It is a safe visual-only port of the original presentation concept.

## Included

- `/orbital mark` — creates a charging beam and target ring about 30 blocks in the direction the command user is looking.
- `/orbital help` — shows the command hint.
- Server-side particle and sound effects.
- Permission level 2 (operators) required.

## Not included

The original Paper plugin's TNT cannons, mass explosions, block destruction, entity damage, and weapon-item firing are not ported. This version only creates visual effects and cannot damage players, mobs, or terrain.

## Build requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.161.0+26.2
- Java 25
- Gradle 9.5.1 or newer

Run from this folder:

```sh
gradle build
```

The mod jar will be in `build/libs/`. Put that jar in the **server's** `mods` folder. Fabric API must also be installed on the server.

## Notes

This source project was prepared against Fabric's 26.2 toolchain versions. It has not been compiled or launched in this environment because only Java 21 is available here and external dependency downloads are unavailable. Use Java 25 when building.
