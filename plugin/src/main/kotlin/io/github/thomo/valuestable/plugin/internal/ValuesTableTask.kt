package io.github.thomo.valuestable.plugin.internal

import io.github.thomo.valuestable.model.ValueCollector
import io.github.thomo.valuestable.plugin.ValueReader
import io.github.thomo.valuestable.printer.Generator
import io.github.thomo.valuestable.printer.HtmlGenerator
import io.github.thomo.valuestable.printer.MarkdownGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ValuesTableTask : DefaultTask() {

	@Input
	val format: Property<String> = project.objects.property(String::class.java)

	@Input
	val target: Property<String> = project.objects.property(String::class.java)

	@OutputFile
	val outputMarkdown: RegularFileProperty = project.objects.fileProperty()

	@OutputFile
	val outputHtml: RegularFileProperty = project.objects.fileProperty()

	@TaskAction
	fun action() {
		val extension = project.extensions.run {
			findByName("valuesTable") as ValuesTableExtension
		}

		val collector = collectValues(extension.files.toList())

		extracted(collector, createGenerator("markdown"), outputMarkdown.get().asFile)
		extracted(collector, createGenerator("html"), outputHtml.get().asFile)
	}

	private fun extracted(collector: ValueCollector, generator: Generator, file: File) {
		writeOutput(generator.generate(collector), file)
		println("Overview generated at ${file.absolutePath}")
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
		return ValueCollector().apply {
			sources.forEach { src ->
				this.add(src.name, reader.read(project.projectDir.toPath().resolve(src.file)))
			}
		}
	}

}
