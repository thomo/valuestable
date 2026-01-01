package io.github.thomo.valuestable.plugin.internal

import io.github.thomo.valuestable.model.ValueCollector
import io.github.thomo.valuestable.plugin.ValueReader
import io.github.thomo.valuestable.printer.Generator
import io.github.thomo.valuestable.printer.HtmlGenerator
import io.github.thomo.valuestable.printer.MarkdownGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ValuesTableTask : DefaultTask() {

	@Input
	val format: Property<String> = project.objects.property(String::class.java)

	@Input
	val target: Property<String> = project.objects.property(String::class.java)

	@Input
	val sources: ListProperty<NamedFile> = project.objects.listProperty(NamedFile::class.java)

	@Input
	val vtEnvs: Property<String> = project.objects.property(String::class.java).convention("")

	@Input
	val vtPath: Property<String> = project.objects.property(String::class.java).convention("")

	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	fun getInputFiles(): List<File> {
		return sources.get().map { project.file(it.file) }
	}

	@OutputFile
	val outputMarkdown: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputHtml: RegularFileProperty = project.objects.fileProperty()

	@TaskAction
	fun action() {
		val collector = collectValues(sources.get())
		// Set path filter if provided
		val pathValue = vtPath.getOrElse("")
		if (pathValue.isNotEmpty()) {
			collector.setPathFilter(pathValue)
		}

		extracted(collector, createGenerator("markdown"), outputMarkdown.get().asFile)
		extracted(collector, createGenerator("html"), outputHtml.get().asFile)
	}

	private fun extracted(collector: ValueCollector, generator: Generator, file: File) {
		writeOutput(generator.generate(collector), file)
		println("Overview generated at file://${file.absolutePath}")
	}

	private fun createGenerator(format: String) = when (format) {
		"html" -> HtmlGenerator()
		"markdown" -> MarkdownGenerator()
		else -> throw IllegalArgumentException("Unsupported format specification")
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
				this.add(src.name, reader.read(project.projectDir.toPath().resolve(src.file)))
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
