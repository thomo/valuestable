package io.github.thomo.valuestable.model

class ValueCollector() {
	private val names = mutableListOf<String>()
	private val maps = hashMapOf<String, ValueMap>()
	private var pathFilter: String = ""
	private var maskPatterns: List<Regex> = emptyList()

	fun getNames() = names.toList()

	fun add(name: String, vm: ValueMap): ValueCollector {
		maps[name] = vm
		names.add(name)
		return this
	}

	fun setPathFilter(path: String) {
		pathFilter = path
	}

	fun setMask(patterns: List<Regex>) {
		maskPatterns = patterns
	}

	// A masked key's values are hidden in every report; generators still see the real values so
	// they can tell which environments share a value.
	fun isMasked(key: String) = maskPatterns.any { it.containsMatchIn(key) }

	fun keys(): List<String> {
		val allKeys = maps.values.flatMap { it.keys }.toSet().toList().sorted()
		return if (pathFilter.isEmpty()) {
			allKeys
		} else {
			allKeys.filter { key -> key.startsWith(pathFilter) }
		}
	}

	fun getValues(key: String) = names.map { n -> maps[n]!! }.map { vm -> vm[key] }

}
