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
- `build/artifacts/quarker-<version>-universal.zip`
- `build/artifacts/quarker-<version>-universal.tar.gz`
- `build/native/<platform>/...` (native app image)

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
