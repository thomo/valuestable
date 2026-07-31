package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MarkdownGenerator(private val name: String? = null, private val wrap: Int = 0) : Generator {
	override fun generate(collector: ValueCollector): List<String> {
		val keys = collector.keys()

		val result = mutableListOf<String>()
		result.add(if (name.isNullOrBlank()) "# Values" else "# Values '$name'")
		result.add("")
		result.add("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
		result.add("")
		result.add("[ValuesTable Plugin](https://github.com/thomo/valuestable)")
		result.add("")
		result.add("|key|values|")
		result.add("|---|:-----|")
		keys.forEach { key -> result.add(buildLine(key, collector)) }
		return result
	}

	override fun generateMerged(charts: List<Pair<String, ValueCollector>>): List<String> {
		val keySets = charts.map { (_, vc) -> vc.keys().toSet() }
		val globalKeys = keySets.flatten().toSet().sorted()

		val result = mutableListOf<String>()
		result.add(if (name.isNullOrBlank()) "# Values" else "# Values '$name'")
		result.add("")
		result.add("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
		result.add("")
		result.add("[ValuesTable Plugin](https://github.com/thomo/valuestable)")
		result.add("")
		result.add("|key|" + charts.joinToString("|") { it.first } + "|")
		result.add("|---|" + charts.joinToString("|") { ":-----" } + "|")
		globalKeys.forEach { key -> result.add(buildMergedLine(key, charts, keySets)) }
		return result
	}

	override fun fileExtension() = "md"

	private fun buildLine(key: String, vc: ValueCollector) = "|" + key.replace(".", "<wbr>.") + "|" + buildCell(key, vc) + "|"

	private fun buildMergedLine(key: String, charts: List<Pair<String, ValueCollector>>, keySets: List<Set<String>>): String {
		val formattedKey = key.replace(".", "<wbr>.")
		val cells = charts.mapIndexed { index, (_, vc) -> if (key in keySets[index]) buildCell(key, vc) else "" }
		return "|$formattedKey|" + cells.joinToString("|") + "|"
	}

	private fun buildCell(key: String, vc: ValueCollector): String {
		val names = vc.getNames()
		return vc.getValues(key)
			.mapIndexed { index, v -> names[index] + ": " + ValueFormatter.format(v, index, false, wrap) }
			.joinToString("<br/>")
	}
}
