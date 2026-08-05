# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.2] - 2026-08-05

### Added

- `json` output format: set `format = "json"` to generate a machine-readable `overview.json` report
- `format` now accepts a comma-separated list of formats, e.g. `format = "html,json"`, to generate any
  combination of `markdown`, `html`, and `json` in one run
- `-PvtFormat` CLI parameter to override the configured `format` for a single invocation, e.g. to force a
  `json` report on demand without editing the build script

### Removed

- `format = "both"` — leaving `format` unset now has the same effect (generates markdown and html)

## [1.6.1] - 2026-08-02

### Added
- support gradle configuration cache

## [1.6.0] - 2026-07-28

### Added

- support of multiple services
- `format` property now actually restricts generation to `markdown` or `html` only (previously accepted but ignored, always generating both)

### Changed

- gradle 9.6.1
- kotlin 2.4.10

## [1.5.2] - 2026-04-30

### Changed

- kotlin 2.3.21
- jackson 2.21.2

## [1.5.1j17] - 2026-01-31

- a Java 17 compatible version of the plugin

## [1.5.1] - 2026-01-01

### Changed

- Print generated file names as uri

## [1.5.0] - 2025-11-23

### Added

- Add environment filtering to the `valuesTable` task, allowing selection of environments via `-PvtEnvs` while always
  including 'default'.
- Add path filtering to the `valuesTable` task, allowing selection of paths via `-PvtPath`.

## [1.4.3] - 2025-11-23

### Added

- Improve HTML header styling and add plugin link to generated outputs.
- Add z-index to sticky table headers for proper layering.
- Preserve and apply the declaration order of source files in the plugin extension.
- Add visual styling to HTML output to differentiate default and environment-specific configuration values.
- Add word break tags before dots in keys for Markdown and HTML output.
- Add client-side table filtering with search input and row count display.
- Add support for null values, centralize value formatting, and refresh HTML report styling.

### Documentation

- Add a comprehensive development guide for the plugin.
- Update README with detailed installation, configuration, usage, and output examples for the plugin.

## [1.4.2] - 2025-11-22

### Fixed

- Detected changed input files and regenerate reports when running `./gradlew valuesTable`.

### Changed

- Upgrade Kotlin to 2.2.21, Java to 21, and Gradle to 8.10.2, pluginPublish plugin version to 2.0.0.
