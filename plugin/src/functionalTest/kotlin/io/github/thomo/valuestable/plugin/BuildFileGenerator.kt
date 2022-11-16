package io.github.thomo.valuestable.plugin

class BuildFileGenerator {

	private var format: String? = null
	private var target: String? = null

	fun target(value: String): BuildFileGenerator {
		target = value
		return this
	}

	fun format(value: String): BuildFileGenerator {
		format = value
		return this
	}

	fun build(): String {
		var result = mutableListOf<String>()
		result.add("plugins { id('io.github.thomo.valuestable') }")
		result.add("valuesTable {")
		createTargetOption()?.run { result.add(this) }
		createFormatOption()?.run { result.add(this) }
		result.add(createFilesSection())
		result.add("}")
		return result.joinToString("\n")
	}

	private fun createFormatOption() = format?.run { "  format = $this" }

	private fun createTargetOption() = target?.run { "  target = $this" }

	private fun createFilesSection() = """
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
"""

}
