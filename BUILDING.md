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
- On pushed tags matching `v*`: creates a GitHub Release and attaches the artifacts.

### Typical release flow

1. Create and push a tag like `v0.7.0`.
2. GitHub Actions builds artifacts.
3. A GitHub Release is created automatically with attached files.
