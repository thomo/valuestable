package io.github.thomo.valuestable.printer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.thomo.valuestable.model.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JsonGenerator(private val name: String? = null) : Generator {
	private val mapper = ObjectMapper()

	override fun generate(collector: ValueCollector): List<String> {
		val root = mapper.createObjectNode()
		if (!name.isNullOrBlank()) root.put("name", name)
		root.put("generatedAt", generatedAt())

		val values = root.putArray("values")
		collector.keys().forEach { key ->
			val entry = values.addObject()
			entry.put("key", key)
			entry.set<ObjectNode>("values", buildValuesNode(key, collector))
		}

		return render(root)
	}

	override fun generateMerged(charts: List<Pair<String, ValueCollector>>): List<String> {
		val keySets = charts.map { (_, vc) -> vc.keys().toSet() }
		val globalKeys = keySets.flatten().toSet().sorted()

		val root = mapper.createObjectNode()
		if (!name.isNullOrBlank()) root.put("name", name)
		root.put("generatedAt", generatedAt())
		val chartNames = root.putArray("charts")
		charts.forEach { (chartName, _) -> chartNames.add(chartName) }

		val values = root.putArray("values")
		globalKeys.forEach { key ->
			val entry = values.addObject()
			entry.put("key", key)
			val chartsNode = entry.putObject("charts")
			charts.forEachIndexed { index, (chartName, vc) ->
				if (key in keySets[index]) {
					chartsNode.set<ObjectNode>(chartName, buildValuesNode(key, vc))
				} else {
					chartsNode.putNull(chartName)
				}
			}
		}

		return render(root)
	}

	override fun fileExtension() = "json"

	// Values were parsed from the source YAML/JSON via `JsonNode.toString()`, so re-parsing them
	// here restores their original JSON type (string, number, boolean, ...) instead of nesting
	// them as an escaped string.
	private fun buildValuesNode(key: String, vc: ValueCollector): ObjectNode {
		val node = mapper.createObjectNode()
		val names = vc.getNames()
		vc.getValues(key).forEachIndexed { index, v ->
			if (v == null) node.putNull(names[index]) else node.set<JsonNode>(names[index], mapper.readTree(v))
		}
		return node
	}

	private fun generatedAt() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

	private fun render(root: ObjectNode) = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root).lines()
}
