# Quarker

Quarker is a fast, turn-based ASCII roguelike where you play as a particle moving through unstable spacetime, fighting color-charged quarks, and growing by absorbing what you defeat.

If you build games, Quarker is interesting because it does not just reskin fantasy roguelike conventions. Its verbs, stats, enemies, and level transitions are built around particle-physics flavor: mass instead of HP, spacetime levels instead of dungeon floors, and wormholes instead of stairs.

## Why Quarker Feels Different

- Particle identity over fantasy class identity:
  - You are a particle avatar, not a warrior/wizard archetype.
- Physics-themed combat language with real gameplay impact:
  - Combat and progression are framed in mass, penetration, deflection, and absorption.
- Growth by absorption:
  - Defeating enemies increases your size (XP), and size drives level-up thresholds.
- Quark ecosystem instead of generic monsters:
  - Enemies include up/down/strange/charm/beauty/truth quarks and anti-variants with chromatic identity.
- Spacetime traversal:
  - Level transitions are wormholes using > and <, with explicit directionality.
- Practical old-school architecture, modernized runtime:
  - The project keeps a clean Java/Swing ASCII loop and now builds/runs on JDK 25.

## Quick Start

Requirements:
- Java 25+
- make

Build:
- make build

Run:
- make run

Package release artifacts:
- make package

## How To Play

Your goal:
- Survive, increase your size by absorbing quarks, and progress through spacetime levels.

Core loop:
- Move, collide, and fight in turns.
- Enemies act after your turn.
- If your mass drops below 1, the run ends.

Movement:
- Arrow keys or roguelike vi keys:
  - h j k l for cardinal movement
  - y u b n for diagonal movement
- . to wait a turn

Actions:
- > move through a downward wormhole
- < move through an upward wormhole
- l enter look mode
- Enter in look mode to inspect the tile under the cursor
- ? show help (placeholder in current build)
- Esc exit the game

Current status notes:
- Saving and loading are currently disabled in this build.

## Legend

- @ player particle
- . floor
- # fold wall/obstacle
- > and < wormholes
- ! item (for example, gluon pickups)
- Lettered quarks such as u, d, s, c, b, t are enemy particles

## For Fellow Game Developers

Quarker is a good reference project if you are interested in:

- Building a readable turn engine around a tiny set of input verbs.
- Creating a distinctive theme by changing systems language, not just art.
- Running an ASCII-style game UI on modern Java with a lightweight Swing console layer.
- Keeping CI and release packaging simple with make + GitHub Actions.

## Releases

Latest release:
- https://github.com/SquidPony/quarker/releases/latest

All releases:
- https://github.com/SquidPony/quarker/releases

## License

No license file is currently included in this repository. If you plan to redistribute or fork commercially, confirm licensing with the project owner first.
