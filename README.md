## ValuesTable

Generates an overview of values in markdown and html format. The report files are generated in `build/valuestable`
folder.

## Plugin usage

Example:

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

### Options

- `files` - files which content will be compared
