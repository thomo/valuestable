package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class ValuesTableExtension(project: Project) {
	val format: Property<String> = project.objects.property(String::class.java).convention("both")

	// No convention here: the default differs depending on whether `charts { }` is used, so
	// each consumer in ValuesTablePlugin supplies its own mode-appropriate fallback.
	val target: Property<String> = project.objects.property(String::class.java)

	val vtEnvs: Property<String> = project.objects.property(String::class.java).convention("")

	val vtPath: Property<String> = project.objects.property(String::class.java).convention("")

	// Only relevant when `charts { }` is used: restricts which registered charts get built.
	// Empty means "all charts". Set from the `-PvtCharts` project property in ValuesTablePlugin.
	val vtCharts: Property<String> = project.objects.property(String::class.java).convention("")

	// Only relevant when `charts { }` is used: instead of one report per chart, produce a
	// single report with one key column (union of all charts' keys) and one value column per
	// chart.
	val mergeCharts: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

	// Column-widening long values (e.g. base64 blobs, URLs) get a <wbr> break opportunity every
	// `wrap` characters. `wrap = 0` disables wrapping.
	val wrap: Property<Int> = project.objects.property(Int::class.java).convention(80)

	val files: NamedDomainObjectContainer<NamedFile> = project.objects.domainObjectContainer(NamedFile::class.java).also { container ->
		container.whenObjectAdded { file ->
			fileOrder.add(file.name)
		}
	}

	private val fileOrder = mutableListOf<String>()

	fun getFilesInOrder(): List<NamedFile> {
		return fileOrder.mapNotNull { name -> files.findByName(name) }
	}

	val charts: NamedDomainObjectContainer<ValuesChartSpec> =
		project.objects.domainObjectContainer(ValuesChartSpec::class.java) { name ->
			project.objects.newInstance(ValuesChartSpec::class.java, name, project)
		}.also { container ->
			container.whenObjectAdded { spec ->
				chartOrder.add(spec.name)
			}
		}

	private val chartOrder = mutableListOf<String>()

	fun getChartsInOrder(): List<ValuesChartSpec> {
		return chartOrder.mapNotNull { name -> charts.findByName(name) }
	}

	// Applies the `-PvtCharts` filter (comma-separated chart names) to the registered charts,
	// preserving registration order. An empty filter selects every chart.
	fun getSelectedChartsInOrder(vtCharts: String): List<ValuesChartSpec> {
		val allCharts = getChartsInOrder()
		if (vtCharts.isEmpty()) return allCharts

		val requestedCharts = vtCharts.split(",").map { it.trim() }.toSet()
		return allCharts.filter { spec -> spec.name in requestedCharts }
	}
}
