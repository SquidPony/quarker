# Quarker

Turn-based ASCII roguelike combat in a particle-physics universe.

You are not a knight in a dungeon. You are a particle in unstable spacetime, absorbing quarks to grow and survive.

![Quarker gameplay recording](screenshots/quarker-recording-20260508-040120-072.gif)

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

Build the browser bundle:

```bash
make web
```

Serve the browser bundle locally:

```bash
make web-serve
```

Build release bundles plus native executable app image(s):

```bash
make package-all
```

Build a native executable app image for your current OS:

```bash
make package-native
```

The generated executables are placed under `build/native/<platform>/`.
The browser bundle is written to `build/web/quarker-<version>/` and can be archived with `make package-web`.

Web build notes:
- The browser target compiles the game to Java 17 bytecode for CheerpJ compatibility while the desktop build remains on Java 25.
- The page is locked down with a restrictive CSP and the local dev server binds to `127.0.0.1` only.
- Save/load, screenshots, and recording are intentionally disabled in the browser build.
- The browser runtime requires a local web server with HTTP Range support, which `make web-serve` provides.

WSL note:
- On WSL, `make package-native` builds Linux output and also tries Windows output if `jpackage.exe` is available on `PATH`.

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
- `p` save screenshot and print saved path.
- `P` save screenshot silently (no message).
- `V` start/stop turn recording; stopping exports an animated GIF.
- `?` help (currently placeholder).
- `Esc` exits the game.

Notes:
- Screenshots and recordings are saved to `screenshots/` in the working directory.
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
