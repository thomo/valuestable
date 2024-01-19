# ValuesTable

Generates an overview of values in markdown and html format. The report files are generated in `build/valuestable`
folder.

## Plugin usage

Add the following to your `build.gradle`:

``` groovy
plugins {
  id('io.github.thomo.valuestable')
}

valuesTable {
  files {
    'default' {
      file = "testdata/values.yaml"
    }
    test.file = "testdata/values-test.yaml"
    dev.file = "testdata/values-dev.yaml"
  }
}
```

Generate the report:

```bash
$ ./gradlew valuesTable
> Task :valuesTable
Overview generated at /Users/thomo/demo/build/valuesTable/overview.md
Overview generated at /Users/thomo/demo/build/valuesTable/overview.html

BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
```

### Options

- `files` - files which content will be compared
