package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector

interface Generator {
	fun generate(collector: ValueCollector): List<String>

	fun fileExtension(): String
}
