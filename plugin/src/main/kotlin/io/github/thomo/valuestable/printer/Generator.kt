package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector

interface Generator {
	fun generate(collector: ValueCollector): List<String>

	// One row per key (union across all charts), one column per chart in the given order. A
	// chart's cell is empty when the chart has no value for that key at all.
	fun generateMerged(charts: List<Pair<String, ValueCollector>>): List<String>

	fun fileExtension(): String
}
