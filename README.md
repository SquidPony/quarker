# Quarker

Turn-based ASCII roguelike combat in a particle-physics universe.

You are not a knight in a dungeon. You are a particle in unstable spacetime, absorbing quarks to grow and survive.

[Latest Release](https://github.com/SquidPony/quarker/releases/latest) | [All Releases](https://github.com/SquidPony/quarker/releases)

## What Makes Quarker Different

- Theme drives systems, not just flavor text:
  - Mass instead of HP, spacetime levels instead of dungeon floors, wormholes instead of stairs.
- Progression through absorption:
  - Defeated quarks increase your size (XP), and size gates level-up thresholds.
- Distinct enemy ecosystem:
  - Up/down/strange/charm/beauty/truth quarks plus anti-variants with chromatic identity.
- Compact but expressive architecture:
  - Old-school ASCII game loop, modernized to run/build on JDK 25.

## At A Glance

| Category | Details |
| --- | --- |
| Genre | Turn-based ASCII roguelike |
| Runtime | Java 25+ |
| Rendering | Swing console interface |
| Build | make |
| Core Fantasy | Particle combat in spacetime |

## Quick Start

```bash
make build
make run
```

Package release artifacts:

```bash
make package
```

## How To Play

Goal:
- Survive, absorb quarks, and advance through spacetime levels.

Turn loop:
- You act, then enemies act.
- If your mass drops below 1, your run ends.

Movement:
- Arrow keys, or vi keys `h j k l`.
- Diagonals with `y u b n`.
- `.` waits one turn.

Actions:
- `>` go through a downward wormhole.
- `<` go through an upward wormhole.
- `l` enter look mode.
- `Enter` in look mode inspects the targeted tile.
- `?` help (currently placeholder).
- `Esc` exits the game.

Current build notes:
- Save/load are currently disabled.

## Symbol Legend

- `@` player particle
- `.` floor
- `#` fold wall
- `>` `<` wormholes
- `!` item pickup (for example, gluon)
- `u d s c b t` quark enemies (plus anti-variants)

## Developer Angle

Quarker is useful if you want to study:

- A readable turn engine with a small, high-signal input vocabulary.
- How to build a unique game identity by changing mechanics language and progression semantics.
- Practical ASCII rendering and input handling in modern Java/Swing.
- Lightweight CI/release packaging with make and GitHub Actions.

## Visual Preview

I attempted to capture a live in-game screenshot in this environment, but desktop capture returns a black frame for Swing windows here. If you run locally, I can add your screenshot in a follow-up commit.

## License

No license file is currently included in this repository. Confirm licensing with the project owner before redistribution.
