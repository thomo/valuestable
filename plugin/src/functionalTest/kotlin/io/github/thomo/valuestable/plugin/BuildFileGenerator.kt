package io.github.thomo.valuestable.plugin

class BuildFileGenerator {

	private var format: String? = null
	private var target: String? = null

	fun build(): String {
		val result = mutableListOf<String>()
		result.add("plugins { id('io.github.thomo.valuestable') }")
		result.add("valuesTable {")
		createTargetOption()?.run { result.add(this) }
		result.add(createFilesSection())
		result.add("}")
		return result.joinToString("\n")
	}

	private fun createTargetOption() = target?.run { "  target = \"$this\"" }

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
