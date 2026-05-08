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

Artifacts are written to:

- `build/dist/quarker-<version>/quarker.jar`
- `build/artifacts/quarker-<version>.zip`
- `build/artifacts/quarker-<version>.tar.gz`

You can override the version label:

- `make package VERSION=v0.7.0`

## GitHub automation

The workflow in `.github/workflows/build-and-release.yml` does the following:

- On every push: builds ZIP/TAR release bundles and uploads them as workflow artifacts.
- On every push to `main`: computes the next numeric release tag, starting at `1.0`, creates the tag on the pushed commit, and publishes a GitHub Release with the built artifacts.

### Typical release flow

1. Push or merge a commit to `main`.
2. GitHub Actions finds the highest existing numeric tag and picks the next one (`1.0`, `1.1`, `1.2`, ...).
3. The workflow builds the release bundles, tags that commit, and creates the GitHub Release with the attached files.
