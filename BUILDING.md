# Building and Running Quarker

## Requirements

- Java 25 or newer
- `make`

## Local development

- Build classes:
  - `make build`
- Run the game:
  - `make run`
- Build a runnable JAR + release bundles:
  - `make package`
- Build the browser bundle:
  - `make web`
- Serve the browser bundle locally with an HTTP Range-capable server:
  - `make web-serve`
- Archive the browser bundle for distribution:
  - `make package-web`
- Build release bundles + native executable app image(s):
  - `make package-all`
- Build an OS-native executable app image (macOS/Linux/Windows, on that OS):
  - `make package-native`
- Build an explicit platform app image:
  - `make package-native-linux`
  - `make package-native-macos`
  - `make package-native-windows`

Artifacts are written to:

- `build/dist/quarker-<version>/quarker.jar`
- `build/web/quarker-<version>/`
- `build/artifacts/quarker-<version>-universal.zip`
- `build/artifacts/quarker-<version>-universal.tar.gz`
- `build/artifacts/quarker-<version>-web.zip`
- `build/native/<platform>/...` (native app image)

### Browser build behavior

- `make web` compiles Java sources with `javac --release 17` for CheerpJ compatibility.
- `make web-serve` binds only to `127.0.0.1` and supports HTTP Range requests, which CheerpJ requires to read the JAR efficiently.
- The browser build disables save/load, screenshots, and recording because those desktop-oriented file operations are not exposed in the hosted runtime.
- The generated page uses a restrictive Content Security Policy that only allows the local bundle and the pinned CheerpJ runtime origin.

### WSL behavior

- On WSL, `make package-native` runs `make package-native-wsl`.
- It always builds Linux output.
- It also builds Windows output when `jpackage.exe` is available on `PATH`.

You can override the version label:

- `make package VERSION=v0.7.0`

## GitHub automation

The workflow in `.github/workflows/build-and-release.yml` does the following:

- On every push: builds ZIP/TAR release bundles and uploads them as workflow artifacts.
- On every push: builds native app images on Linux, macOS, and Windows, then uploads them as workflow artifacts.
- On every push to `main`: computes the next numeric release tag, starting at `1.0`, creates the tag on the pushed commit, and publishes a GitHub Release with the built artifacts.

### Typical release flow

1. Push or merge a commit to `main`.
2. GitHub Actions finds the highest existing numeric tag and picks the next one (`1.0`, `1.1`, `1.2`, ...).
3. The workflow builds the release bundles, tags that commit, and creates the GitHub Release with the attached files.
