# ValuesTable

![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.thomo.valuestable)

Generates an overview of Helm values in Markdown, HTML, and JSON format. The report files are generated in the
`build/valuesTable` folder.

## Requirements

- Java 21 or higher
- Gradle 9.x

## Installation

### Kotlin DSL

```kotlin
plugins {
	id("io.github.thomo.valuestable") version "<version>"
}
```

### Groovy DSL

```groovy
plugins {
    id "io.github.thomo.valuestable" version "<version>"
}
```

## Configuration

Configure the plugin in your `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy).

### Kotlin DSL

```kotlin
valuesTable {
	// Optional: Output directory (default: build/valuesTable)
	target.set("build/my-overview")

	// Optional: Output format - unset (default), or a comma-separated list of one or more of
	// "markdown", "html", "json".
	// Leaving it unset generates the markdown and html reports; "json" is only generated
	// when selected explicitly.
	// Restrict to a single format, e.g. to generate only the HTML report:
	format.set("html")
	// Or combine formats, e.g. to generate html and json but skip markdown:
	// format.set("html,json")

	// Optional: insert a break opportunity every N characters in long values (default: 80),
	// so values with no natural break point (base64 blobs, long URLs, ...) don't force wide
	// table columns. Set to 0 to disable wrapping.
	wrap.set(80)

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
    // Optional: Output format - unset (default), or a comma-separated list of one or more of
    // "markdown", "html", "json".
    // Leaving it unset generates the markdown and html reports; "json" is only generated
    // when selected explicitly.
    // Restrict to a single format, e.g. to generate only the HTML report:
    format = "html"
    // Or combine formats, e.g. to generate html and json but skip markdown:
    // format = "html,json"

    // Optional: insert a break opportunity every N characters in long values (default: 80),
    // so values with no natural break point (base64 blobs, long URLs, ...) don't force wide
    // table columns. Set to 0 to disable wrapping.
    wrap = 80

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

### Generating separate tables per chart (e.g. per subchart)

If your Helm chart has subcharts and you want one overview table per subchart instead of a single merged table, register
each one under `charts { }` instead of the top-level `files { }`:

```kotlin
valuesTable {
	// Optional: output folder for chart reports (default: build/valuesTable).
	// Each chart's .md/.html files are named after its registration name within this folder,
	// e.g. with the override below: build/my-charts/serviceA.md, build/my-charts/serviceB.md
	target.set("build/my-charts")

	charts {
		register("serviceA") {
			files {
				register("default") { file = "charts/serviceA/values.yaml" }
				register("dev") { file = "charts/serviceA/values-dev.yaml" }
			}
		}
		register("serviceB") {
			files {
				register("default") { file = "charts/serviceB/values.yaml" }
				register("dev") { file = "charts/serviceB/values-dev.yaml" }
			}
		}
	}
}
```

`target` and `format` are configured once, at the same level as `charts { }`, and apply to every chart. Each chart entry
only needs its own `files { }`. Every entry gets its own generation task, named `valuesTable<Name>` (e.g.
`valuesTableServiceA`, runnable on its own), and the main `valuesTable` task depends on every registered entry, so
`./gradlew valuesTable` builds all of them in one go. When `charts { }` is used, `valuesTable` itself only drives those
sub-tasks and is skipped otherwise — it does not additionally generate a flat/default report from the (empty)
top-level `files { }`.

**The top-level `files { }` and `charts { }` are mutually exclusive** — configure a project with one or the other, not
both. If both contain entries, the build fails with an explanatory error. Projects using only the top-level `files { }`
(the pre-existing DSL) are unaffected by this feature; `charts { }` defaults to empty and adding a plugin version with
this feature changes nothing until you opt in.

### Merging all charts into a single report

If you'd rather have one overview file that compares all charts side by side instead of a separate file per chart, set
`mergeCharts` alongside `charts { }`:

```kotlin
valuesTable {
	target.set("build/my-charts/overview")
	mergeCharts.set(true)

	charts {
		register("serviceA") {
			files {
				register("default") { file = "charts/serviceA/values.yaml" }
			}
		}
		register("serviceB") {
			files {
				register("default") { file = "charts/serviceB/values.yaml" }
			}
		}
	}
}
```

With `mergeCharts` enabled, `valuesTable` produces a single `<target>.md` / `<target>.html` report instead of per-chart
files, and the per-chart sub-tasks (`valuesTableServiceA`, etc.) are no longer wired into the `valuesTable`
task graph — though they still exist and can be run standalone if needed. The report's key column is the union of every
chart's keys; each chart gets its own column, in the order it was registered, showing the same per-environment content
it would have in its own report. If a chart has no value at all for a given key, its cell is left empty.

## Usage

Generate the report by running the `valuesTable` task:

```bash
./gradlew valuesTable
```

### Filtering Environments

You can restrict the output to specific environments using the `-PvtEnvs` parameter. The `default` environment is always
included.

```bash
# Include only default and dev environments
./gradlew valuesTable -PvtEnvs=dev

# Include default, dev, and test environments
./gradlew valuesTable -PvtEnvs=dev,test

# Include all environments (no filter)
./gradlew valuesTable
```

### Filtering by Path

You can restrict the output to keys that start with a specific path using the `-PvtPath` parameter.

```bash
# Include only keys starting with "root.subkey"
./gradlew valuesTable -PvtPath=root.subkey

# Include only keys starting with "root"
./gradlew valuesTable -PvtPath=root

# Combine path and environment filters
./gradlew valuesTable -PvtPath=root.config -PvtEnvs=dev,test
```

### Filtering Charts

When using `charts { }`, you can restrict `valuesTable` to specific charts using the `-PvtCharts` parameter — useful for
only generating (or merging) the reports you currently need instead of every registered chart.

```bash
# Only build the serviceB and serviceC charts (or, with mergeCharts, only include them in the merged report)
./gradlew valuesTable -PvtCharts=serviceB,serviceC

# Build all registered charts (no filter)
./gradlew valuesTable
```

`-PvtCharts` only affects the aggregate `valuesTable` task; a chart's own sub-task (e.g. `valuesTableServiceA`) can
always be run standalone regardless of the filter.

### Overriding the format from the CLI

You can force a different format than the one configured in the build script using the `-PvtFormat` parameter — useful
to have a build script default to e.g. `html` for everyday human use, while still letting a one-off invocation (a CI
step, or an agent that wants to parse the report) request `json` on demand without editing the build script. Accepts
the same comma-separated syntax as `format`, and overrides it completely for that invocation.

```bash
# Build script is configured with format = "html"; this run generates only json instead
./gradlew valuesTable -PvtFormat=json

# Generate html and json in one run, regardless of what's configured
./gradlew valuesTable -PvtFormat=html,json

# No override -> uses whatever `format` is set to in the build script
./gradlew valuesTable
```

Output:

```text
> Task :valuesTable
Overview generated at file:///path/to/project/build/valuesTable/overview.md
Overview generated at file:///path/to/project/build/valuesTable/overview.html

BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed
```

## Output

By default the plugin generates two files:

1. `overview.md`: A Markdown table comparing the values.
2. `overview.html`: An HTML table comparing the values.

Setting `format = "json"` instead generates `overview.json`, a machine-readable report with the same data:

```json
{
  "generatedAt" : "2024-01-01 12:00:00",
  "values" : [
    {
      "key" : "root.a",
      "values" : {
        "default" : "aaa",
        "dev" : null,
        "test" : null
      }
    },
    {
      "key" : "root.c",
      "values" : {
        "default" : "ccc",
        "dev" : null,
        "test" : "cTest"
      }
    }
  ]
}
```

Each entry's `values` object maps every environment name to its value, in its native JSON type; `null` means that
environment has no value of its own for that key and falls back to `default`. When `mergeCharts` is used, entries have
a `charts` object instead, mapping chart name to that chart's `values` object (or `null` if the chart has no value at
all for that key).

### Example Output

| key      | values                                                |
|:---------|:------------------------------------------------------|
| `root.a` | default: "aaa"<br/>dev: *default*<br/>test: *default* |
| `root.c` | default: "ccc"<br/>dev: null<br/>test: "cTest"        |
