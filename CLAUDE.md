# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## System Prompt

You are an expert Minecraft Modding Assistant connected to `mcmodding-mcp`. **DO NOT rely on your internal knowledge** for modding APIs (Fabric/NeoForge) as they change frequently.
**ALWAYS** use the available tools:

 - `search_fabric_docs` and `get_example` for documentation and code patterns
 - `search_mappings` and `get_class_details` for Minecraft internals and method signatures
 - `search_mod_examples` for battle-tested implementations from popular mods

 Prioritize working code examples over theoretical explanations. When dealing with Minecraft internals, use the mappings tools to get accurate parameter names and Javadocs. If the user specifies a Minecraft version, ensure all retrieved information matches that version.

## Build & Run

```bash
./gradlew build                    # Build mod jar
./gradlew runClient                # Launch Minecraft client with mod
./gradlew runServer                # Launch dedicated server with mod
bash scripts/run_cave_test.sh      # Automated test: build → server → force-load chunks → JSON report
```

There are no unit tests. Validation is done via the RCON-based server pipeline (`scripts/run_cave_test.sh`) which parses `[CAVEDATA]` log lines into statistical reports at `test_results/`. See `TESTING.md` for details.

## Project Stack

- **Java 25**, Gradle 9.2, Fabric Loom 1.15-SNAPSHOT
- **Minecraft 26.1.1**, Fabric Loader 0.18.6, Fabric API 0.145.2
- Versions managed in `gradle.properties`
- Cloth Config is `compileOnly` (optional at runtime, for ModMenu config screen)
- FastNoiseLite is vendored in `wftech.caveoverhaul.fastnoise` — not an external dep

## Architecture

All source lives under `src/main/java/wftech/caveoverhaul/`.

**Entry point**: `CaveOverhaul` implements `ModInitializer` — loads config, registers carvers, hooks server lifecycle.

**Mixins are the core integration mechanism** (configured in `caveoverhaul.mixins.json`):
- `RandomStateMixin` — intercepts `RandomState` construction to replace vanilla noise settings via `NoiseMaker`
- `NoiseChunkMixin` — intercepts block placement during chunk gen, delegates to `NoiseChunkMixinUtils` which queries layer holders for cave/river carving
- `IntegratedServerMixin` / `DedicatedServerMixin` — capture server reference on startup
- `DripstoneUtilsMixin` — prevents dripstone generation inside river zones

**Three generation systems**, all noise-driven via FastNoiseLite:

1. **Noise caves** (`carvertypes/`): large spelunking caverns meant for exploration, generated in horizontal layers. `NCLayerHolder` (singleton) creates 8 `NCDynamicLayer` instances spanning **Y=-64 to Y=128** across three tiers: top (96-128, 64-96, 32-64), middle (0-32, -32-0), and bottom (-64-0, -64-0, -54--64). Bottom layers are intentionally duplicated with different seed offsets to produce denser caves at depth. If the world extends below Y=-64, extra doubled-up layers are added in 64-block increments down to minY. Each layer uses `NCLogic` to compute a cave Y-level and height from noise (max height 12 blocks, shaped by a sigmoid), then `NCDynamicLayer` checks structural noise (threshold >0.15) with domain warping to decide whether to carve. Lower Y = stronger domain warping for more twisted shapes.

2. **Underground rivers** (`carvertypes/rivers/`): `NURLayerHolder` (singleton) → 3 `NURDynamicLayer` water layers at fixed Y-levels: **Y=-12, Y=0, Y=12**. Each layer determines liquid (water), air (carved above water), support stone (below water), and boundary stone placement via `NURLogic` noise. The `DripstoneUtilsMixin` prevents dripstone from generating inside river zones.

3. **Legacy carvers** (`carvertypes/`), registered via `InitCarverTypesFabric`:
   - **`OldWorldCarverv12`** — pre-1.18 labyrinthine tunnel-and-room caves. Generates room clusters (2-5 rooms) connected by branching tunnels of 50-89 nodes. Tunnel pitch is clamped to ±60° (`±π/3`), so tunnels trend downward at a gentle angle (~30° average descent) rather than dropping vertically — this is intentional so players can comfortably descend them. Has two generation passes: deep caves (biased toward low Y via nested `nextInt`) and shallow caves (biased toward Y=80-120). Shallow caves have a 30% chance of surface entrances, which force a downward pitch bias (-0.05 to -0.2 radians). Tunnels avoid carving through river zones.
   - **`VanillaCanyon`** — ravines, split into two types distinguished by `yScale.min()`: **upper canyons** (Y from minY to 180) and **lower canyons** (Y from minY to 25% of minY, i.e. the deepest quarter). Each type has independent spawn chance and debug toggle in config.

**Block determination order** (in `NoiseChunkMixinUtils`): river liquid → river air → river support stone → cave air.

**Thread safety**: layer holder singletons use double-checked locking; `Globals` uses atomics; river layers use thread-local warp caches. All singletons reset on server shutdown.

**Config**: custom file-based config at `config/caveoverhaul.cfg`, managed by `Config.java`. Controls spawn chances, air exposure, lava offset, debug toggles, and feature enables.

**Access widener** (`caveoverhaul.accesswidener`) opens many private Minecraft world-gen internals (density functions, NoiseChunk caches, ore vein types).

**Domain warping**: `NoisetypeDomainWarp` (singleton) provides coordinate warping shared by caves and canyons for natural-looking variation.

## Data-Driven Carver Configuration

All configured carver JSONs live under `src/main/resources/data/`. The mod uses a two-part strategy: override vanilla carvers to do nothing, then register custom replacements via Fabric's `BiomeModifications.addCarver()` in `InitCarverTypesFabric`.

**Vanilla overrides** (`data/minecraft/worldgen/configured_carver/`) — these replace Minecraft's built-in configured carvers by matching their resource keys, setting probability to 0 to disable them entirely:
- `canyon.json` — disables `minecraft:canyon` (probability 0.00)
- `cave.json` — disables `minecraft:cave` (probability 0.0)
- `cave_extra_underground.json` — disables `minecraft:cave_extra_underground` (probability 0.00, Y range capped at 47)

All three retain vanilla's full config structure (debug states, shape params, etc.) but never fire because probability is zero.

**Mod replacements** (`data/caveoverhaul/worldgen/configured_carver/`) — these define the mod's custom carvers, added to all overworld biomes via `InitCarverTypesFabric`:
- `canyons.json` — upper canyons using `caveoverhaul:vanilla_canyon` type. Probability 0.08, Y range 8-above-bottom to 180, **distance factor 1.0–2.0** (2x wider than vanilla's 0.75–1.0), `yScale: 3`. Added to biomes.
- `canyons_low_y.json` — lower canyons using `caveoverhaul:vanilla_canyon` type. Probability 0.04, Y range 8-above-bottom to 48-above-bottom (deep only), vanilla-width distance factor 0.75–1.0, `yScale: 3.01`. The `yScale > 3` is the discriminator that `VanillaCanyon.isStartChunk()` checks to route to lower-canyon logic (config-driven spawn chance, separate debug toggle, Y biased toward the deepest quarter of the world). Added to biomes.
- `caves_noise_distribution.json` — v1.2 tunnel-and-room caves using `caveoverhaul:v12_caves` type. Probability 1.0 (always carves where carver is invoked), full Y range 8-above-bottom to 180. Added to biomes.
- `cave.json` — uses vanilla `minecraft:cave` type with probability 0.15. **Not referenced** by `InitCarverTypesFabric` and not added to any biomes; appears to be an unused/legacy file.

**Registration flow**: `InitCarverTypesFabric.init()` registers two carver types (`caveoverhaul:vanilla_canyon`, `caveoverhaul:v12_caves`) into `BuiltInRegistries.CARVER`, then adds three configured carvers (`canyons`, `canyons_low_y`, `caves_noise_distribution`) to all overworld biomes. Minecraft's data pack loader reads the JSON configs and pairs them with the registered carver types.
