package io.github.thomo.valuestable.plugin.internal

import java.io.Serializable

open class NamedFile constructor(val name: String): Serializable {
	var file: String = ""
}
