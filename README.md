<div align="center">

<img src="https://raw.githubusercontent.com/Sol-Client/client/develop/src/main/resources/assets/sol_client/textures/gui/icon.png" alt="Sol Client's logo">

# Sol Client

An **open source**, non-hacked client for Minecraft 1.8.9 (newer versions coming in the future)

<a href="https://discord.gg/TSAkhgXNbK"><img alt="Join Discord" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_vector.svg"></a>
<a href="https://www.java.com"><img alt="Java" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/java_vector.svg"></a>

</div>

## 📖 Features

- Customisable HUD
- Freelook
- Zoom
- Motion blur
- Menu blur
- 1.7 animations
- Item physics
- Hypixel additions
- OptiFine (optional, from [its website](https://optifine.net/downloads))
- Custom crosshairs
- Quick Play (uses games from robere2's mod)
- Better item tooltips (like the ones in Bedrock Edition)
- Chat symbol picker
- Chat channel picker and status
- Popup events (based on Sk1er's [PopupEvents](https://github.com/Sk1erLLC/PopupEvents))
- Speedometer
- Animated chunks (based on Lumien's [Chunk Animator](https://github.com/lumien231/Chunk-Animator))
- Timers
- Resource pack folders
- Replay Mod (using the actual [Replay Mod](https://github.com/ReplayMod/ReplayMod))
- Entity Culling (using an **outdated version** of tr7zw's [Enity Culling](https://github.com/tr7zw/EntityCulling) mod, since the owner adopted a more restrictive license)

**..and even more!**

## Contributing
If you want to contribute features, use the [`development`](https://github.com/Sol-Client/client) branch. If you want to contribute bug fixes, use the [`stable`](https://github.com/Sol-Client/client/tree/stable) branch.

### Code Formatting

Please use standard Java formatting conventions (the default Eclipse formatting profile, but with indented switch cases).
Using statements instead of blocks is fine.
Use tabs for indentation, and asterisks if more than one class is imported from a package.

You may notice this is not followed on the main branch (as of 2023-01-03 - once [`dev/1.9.0`]() is merged this will not be the case).

### Building

To compile the client, run: `./gradlew build`

To run it, execute the following command: `./gradlew runClient`.

## Testing

Before a new release is created, the following must be tested:

- If Sol compiles
- If it runs in development
- If it runs the first or second time the client is game is launched on any machine
- If it works in normal gameplay, with the new features enabled. This may mean releases take longer, but it is probably worth it
- If the old features still work correctly
- If it plays nicely with Watchdog (and other anticheats)

## License

Sol Client is Free and Open Source Software (FOSS), licensed under the [GNU General Public License](LICENSE), version 3.0
