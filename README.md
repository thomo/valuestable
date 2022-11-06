## Plugin usage

``` groovy
plugins {
	id('io.github.thomo.valuestable')
}

valuesTable {
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
