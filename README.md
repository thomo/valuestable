## Plugin usage

Example:

``` groovy
plugins {
	id('io.github.thomo.valuestable')
}

valuesTable {
    format = "html"
	target = "testdata/overview.md"
	files {
		'default' {
			file = "testdata/values.yaml"
		}
		test {
			file = "testdata/values-test.yaml"
		}
		dev {
			file = "testdata/values-dev.yaml"
		}
	}
}
```

### Options

- `format` - output format, `markdown` (default) and `html` are supported
- `target` - location of target file
- `files` - files which content will be compared
