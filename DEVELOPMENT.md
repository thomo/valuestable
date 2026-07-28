# Development Guide

This guide provides information for developers working on the ValuesTable Gradle plugin.

## Prerequisites

- Java 21 or higher
- Gradle 9.x (wrapper included)

## Project Structure

```
valuestable/
├── plugin/                          # Main plugin module
│   ├── src/main/kotlin/            # Plugin source code
│   │   └── io/github/thomo/valuestable/
│   │       ├── model/              # Data models
│   │       ├── plugin/             # Plugin implementation
│   │       └── printer/            # Output generators
│   ├── src/functionalTest/kotlin/ # Functional tests
│   └── build.gradle.kts            # Plugin build configuration
├── gradle/
│   └── libs.versions.toml          # Version catalog
└── README.md
```

## Building the Plugin

Build the plugin locally:

```bash
./gradlew :plugin:build
```

## Running Tests

Run all tests:

```bash
./gradlew :plugin:test
```

Run functional tests:

```bash
./gradlew :plugin:functionalTest
```

Run a specific test:

```bash
./gradlew :plugin:functionalTest --tests "*TaskBehavior*"
```

## Local Development & Testing

### Option 1: Publish to Maven Local

Publish the plugin to your local Maven repository:

```bash
./gradlew :plugin:publishToMavenLocal
```

Then in another project's `settings.gradle.kts`, add:

```kotlin
pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
	}
}
```

And use the plugin:

```kotlin
plugins {
	id("io.github.thomo.valuestable") version "1.6.0"
}
```

### Option 2: Composite Build

In another project's `settings.gradle.kts`:

```kotlin
includeBuild("/path/to/valuestable")
```

Then apply the plugin normally:

```kotlin
plugins {
	id("io.github.thomo.valuestable")
}
```

### Option 3: Test in This Repository

The plugin can be tested directly in this repository by creating a test configuration in the root `build.gradle.kts`.

## Code Style

- Use Kotlin coding conventions
- Follow existing code patterns
- Keep functions focused and concise
- Add tests for new features

## Dependency Management

Dependencies are managed via the Version Catalog in `gradle/libs.versions.toml`. To update a dependency:

1. Update the version in `libs.versions.toml`
2. Run tests to ensure compatibility
3. Update documentation if needed

## Release Process

1. Update version in `plugin/build.gradle.kts`
2. Run all tests: `./gradlew :plugin:functionalTest`
3. Build the plugin: `./gradlew :plugin:build`
4. Publish to Gradle Plugin Portal: `./gradlew :plugin:publishPlugins`

## Troubleshooting

### Tests Failing

- Ensure Java 21 is being used: `java -version`
- Clean build: `./gradlew clean :plugin:build`
- Check test reports: `plugin/build/reports/tests/functionalTest/index.html`

### Plugin Not Found Locally

- Verify Maven Local publication: `ls ~/.m2/repository/io/github/thomo/valuestable/plugin/`
- Check `settings.gradle.kts` includes `mavenLocal()` in repositories

## Contributing

1. Create a feature branch
2. Make your changes
3. Add/update tests
4. Ensure all tests pass
5. Submit a pull request
