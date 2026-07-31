package io.github.thomo.valuestable.plugin.internal

import io.github.thomo.valuestable.model.ValueCollector
import io.github.thomo.valuestable.plugin.ValueReader
import io.github.thomo.valuestable.printer.HtmlGenerator
import io.github.thomo.valuestable.printer.MarkdownGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

@CacheableTask
open class ValuesTableTask @Inject constructor(
	private val layout: ProjectLayout
) : DefaultTask() {

	@Input
	val format: Property<String> = project.objects.property(String::class.java)

	@Input
	val target: Property<String> = project.objects.property(String::class.java)

	@Input
	val sources: ListProperty<NamedFile> = project.objects.listProperty(NamedFile::class.java)

	// Populated only for the merged-charts report: one entry per registered chart, in
	// registration order. When non-empty, `action()` renders a single merged report instead of
	// treating `sources` as a flat file list.
	@Input
	val chartSources: ListProperty<ChartFiles> = project.objects.listProperty(ChartFiles::class.java)

	@Input
	val vtEnvs: Property<String> = project.objects.property(String::class.java).convention("")

	@Input
	val vtPath: Property<String> = project.objects.property(String::class.java).convention("")

	@Input
	val wrap: Property<Int> = project.objects.property(Int::class.java).convention(80)

	@Input
	@Optional
	val reportName: Property<String> = project.objects.property(String::class.java)

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	fun getInputFiles(): List<File> {
		val flatFiles = sources.get().map { it.file }
		val chartFiles = chartSources.get().flatMap { chart -> chart.files.map { it.file } }
		return (flatFiles + chartFiles).map { layout.projectDirectory.file(it).asFile }
	}

	@Optional
	@OutputFile
	val outputMarkdown: RegularFileProperty = project.objects.fileProperty()

	@Optional
	@OutputFile
	val outputHtml: RegularFileProperty = project.objects.fileProperty()

	@TaskAction
	fun action() {
		val chartsValue = chartSources.get()
		val formatValue = format.getOrElse("both")
		if (formatValue !in setOf("both", "markdown", "html")) {
			throw IllegalArgumentException("Unsupported format specification")
		}

		if (chartsValue.isNotEmpty()) {
			actionMerged(chartsValue, formatValue)
		} else {
			actionFlat(formatValue)
		}
	}

	private fun actionFlat(formatValue: String) {
		val collector = collectValues(sources.get())
		applyPathFilter(collector)

		if (formatValue == "both" || formatValue == "markdown") {
			extracted(createGenerator("markdown", reportName.orNull).generate(collector), outputMarkdown.get().asFile)
		}
		if (formatValue == "both" || formatValue == "html") {
			extracted(createGenerator("html", reportName.orNull).generate(collector), outputHtml.get().asFile)
		}
	}

	private fun actionMerged(chartsValue: List<ChartFiles>, formatValue: String) {
		val charts = chartsValue.map { chart ->
			val collector = collectValues(chart.files)
			applyPathFilter(collector)
			chart.name to collector
		}

		if (formatValue == "both" || formatValue == "markdown") {
			extracted(createGenerator("markdown", null).generateMerged(charts), outputMarkdown.get().asFile)
		}
		if (formatValue == "both" || formatValue == "html") {
			extracted(createGenerator("html", null).generateMerged(charts), outputHtml.get().asFile)
		}
	}

	private fun createGenerator(format: String, name: String?) = when (format) {
		"html" -> HtmlGenerator(name, wrap.getOrElse(80))
		"markdown" -> MarkdownGenerator(name, wrap.getOrElse(80))
		else -> throw IllegalArgumentException("Unsupported format specification")
	}

	private fun applyPathFilter(collector: ValueCollector) {
		val pathValue = vtPath.getOrElse("")
		if (pathValue.isNotEmpty()) {
			collector.setPathFilter(pathValue)
		}
	}

	private fun extracted(lines: List<String>, file: File) {
		writeOutput(lines, file)
		println("Overview generated at file://${file.absolutePath}")
	}

	private fun writeOutput(lines: List<String>, output: File) {
		output.createNewFile()
		output.printWriter().use { pw -> lines.forEach { pw.println(it) } }
	}

	private fun collectValues(sources: List<NamedFile>): ValueCollector {
		val reader = ValueReader()
		val filteredSources = filterSources(sources)
		return ValueCollector().apply {
			filteredSources.forEach { src ->
				this.add(src.name, reader.read(layout.projectDirectory.file(src.file).asFile.toPath()))
			}
		}
	}

	private fun filterSources(sources: List<NamedFile>): List<NamedFile> {
		val envsValue = vtEnvs.getOrElse("")
		if (envsValue.isEmpty()) {
			// No filter specified, include all sources
			return sources
		}

		val requestedEnvs = envsValue.split(",").map { it.trim() }.toSet()
		// Always include 'default' plus any requested environments
		val envsToInclude = requestedEnvs + "default"

		return sources.filter { src -> src.name in envsToInclude }
	}

}
