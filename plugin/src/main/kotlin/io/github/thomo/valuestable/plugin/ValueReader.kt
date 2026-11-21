package io.github.thomo.valuestable.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.thomo.valuestable.model.ValueMap
import java.nio.file.Path

class ValueReader() {
	fun read(fileName: Path): ValueMap {
		val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

		val node = mapper.readTree(fileName.toFile())

		return mutableMapOf<String, String?>().also { result -> node?.let { buildValues(it, "", result) } }
	}

	private fun buildValues(node: JsonNode, prefix: String, result: MutableMap<String, String?>) {
		if (node.isObject) {
			node.properties().forEach { (key, subNode) -> buildValues(subNode, buildKey(prefix, key), result) }
		} else {
			result[prefix] = if (node.isNull) null else node.toString()
		}
	}

	// Escapes `\` and `.` inside a key like Helm's `--set` does, so a literal `a.b` key stays
	// distinguishable from a nested `a: { b }` instead of overwriting it.
	private fun buildKey(prefix: String, key: String): String {
		val escaped = key.replace("\\", "\\\\").replace(".", "\\.")
		return if (prefix.isBlank()) escaped else "$prefix.$escaped"
	}

}
