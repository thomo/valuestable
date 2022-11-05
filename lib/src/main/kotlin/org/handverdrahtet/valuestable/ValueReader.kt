package org.handverdrahtet.valuestable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.nio.file.Path

class ValueReader() {
	fun read(fileName: Path): ValueMap {
		val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

		var node = mapper.readTree(fileName.toFile())

		return node?.run { buildValues(node, "") } ?: emptyValueMap()
	}

	private fun buildValues(node: JsonNode, prefix: String): ValueMap {
		var result = emptyValueMap()
		return if (node.isObject) {
			node.fieldNames().forEach { key ->
				val subNode = node.get(key)
				result = result.toMutableMap().apply { putAll(buildValues(subNode, buildKey(prefix, key))) }
			}
			result
		} else {
			result.toMutableMap().apply { put(prefix, node.toString()) }
		}
	}

	private fun buildKey(prefix: String, key: String) = if (prefix.isBlank()) key else "$prefix.$key"

}
