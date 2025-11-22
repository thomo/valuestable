package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MarkdownGenerator : Generator {
	override fun generate(collector: ValueCollector): List<String> {
		val keys = collector.keys()

		val result = mutableListOf<String>()
		result.add("# Values")
		result.add("")
		result.add("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
		result.add("")
		result.add("|key|values|")
		result.add("|---|:-----|")
		keys.forEach { key -> result.add(buildLine(key, collector)) }
		return result
	}

	override fun fileExtension() = "md"

	private fun buildLine(key: String, vc: ValueCollector): String {
		val names = vc.getNames()

		val formattedKey = key.replace(".", "<wbr>.")
		return "|$formattedKey|" +
			vc.getValues(key)
				.mapIndexed { index, v -> names[index] + ": " + ValueFormatter.format(v, index, false) }
				.joinToString("<br/>", postfix = "|")
	}
}
