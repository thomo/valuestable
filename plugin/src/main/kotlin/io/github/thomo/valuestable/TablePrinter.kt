package io.github.thomo.valuestable

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TablePrinter {
	companion object {
		fun toMarkdown(vc: ValueCollector): List<String> {
			var names = vc.getNames()
			var keys = vc.keys()

			val result = mutableListOf<String>()
			result.add("# Values")
			result.add("")
			result.add("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			result.add("")
			result.add("|key|" + names.joinToString("|", postfix = "|"))
			result.add("|---|" + ":---:|".repeat(names.size))
			keys.forEach { key -> result.add(buildLine(key, vc)) }
			return result
		}

		private fun buildLine(key: String, vc: ValueCollector) =
			"|$key|" +
				vc.getValues(key)
					.mapIndexed { index, v -> v ?: if (index == 0) "*(n.d.)*" else "*default*" }
					.joinToString("|", postfix = "|")

	}
}
