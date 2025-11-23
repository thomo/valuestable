# ValuesTable

![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.thomo.valuestable)

Generates an overview of Helm values in Markdown and HTML format. The report files are generated in the `build/valuesTable` folder.

## Requirements

- Java 21 or higher
- Gradle 8.x

## Installation

### Kotlin DSL

```kotlin
plugins {
  id("io.github.thomo.valuestable") version "1.4.2"
}
```

### Groovy DSL

```groovy
plugins {
  id "io.github.thomo.valuestable" version "1.4.2"
}
```

## Configuration

Configure the plugin in your `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy).

### Kotlin DSL

```kotlin
valuesTable {
    // Optional: Output directory (default: build/valuesTable/overview)
    target.set("build/my-overview")
    
    // Optional: Output format (default: markdown)
    // Currently supports generating both Markdown and HTML automatically.
    
    files {
        register("default") {
            file = "values.yaml"
        }
        register("dev") {
            file = "values-dev.yaml"
        }
        register("test") {
            file = "values-test.yaml"
        }
    }
}
```

### Groovy DSL

```groovy
valuesTable {
  files {
    'default' {
      file = "values.yaml"
    }
    dev {
      file = "values-dev.yaml"
    }
    test {
      file = "values-test.yaml"
    }
  }
}
```

## Usage

Generate the report by running the `valuesTable` task:

```bash
./gradlew valuesTable
```

### Filtering Environments

You can restrict the output to specific environments using the `-Penvs` parameter. The `default` environment is always included.

```bash
# Include only default and dev environments
./gradlew valuesTable -Penvs=dev

# Include default, dev, and test environments
./gradlew valuesTable -Penvs=dev,test

# Include all environments (no filter)
./gradlew valuesTable
```

Output:
```text
> Task :valuesTable
Overview generated at /path/to/project/build/valuesTable/overview.md
Overview generated at /path/to/project/build/valuesTable/overview.html

BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed
```

## Output

The plugin generates two files:
1.  `overview.md`: A Markdown table comparing the values.
2.  `overview.html`: An HTML table comparing the values.

### Example Output

| key | values |
| :--- | :--- |
| `root.a` | default: "aaa"<br/>dev: *default*<br/>test: *default* |
| `root.c` | default: "ccc"<br/>dev: null<br/>test: "cTest" |
