package org.handverdrahtet.valuestable


class ValueCollector() {
	private val names = mutableListOf<String>()
	private val maps = hashMapOf<String, ValueMap>()

	fun getNames() = names.toList()

	fun add(name: String, vm: ValueMap): ValueCollector {
		maps[name] = vm
		names.add(name)
		return this
	}

	fun keys() = maps.values.flatMap { it.keys }.toSet().toList().sorted()

	fun getValues(key: String) = names.map { n -> maps[n]!! }.map { vm -> vm[key] }

}
