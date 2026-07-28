package io.github.thomo.valuestable.plugin.internal

import java.io.Serializable

data class ChartFiles(val name: String, val files: List<NamedFile>) : Serializable
