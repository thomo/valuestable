package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import io.github.thomo.valuestable.TablePrinter
import io.github.thomo.valuestable.ValueCollector
import io.github.thomo.valuestable.ValueReader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val outputFile = "valuesTable/overview.md"

open class ValuesTableTask : DefaultTask() {
	@OutputFile
	var output: File = project.file("${project.buildDir}/$outputFile")

	@TaskAction
	fun action() {
		val extension = project.extensions.run {
			findByName("valuesTable") as ValuesTableExtension
		}

		createOverviewFile(extension.files.toList())
		println("Overview generated at ${output.absolutePath}")
	}

	private fun createOverviewFile(sources: List<NamedFile>) {
		val reader = ValueReader()
		val collector = ValueCollector().apply {
			sources.forEach { src ->
				this.add(src.name, reader.read(project.projectDir.toPath().resolve(src.file)))
			}
		}

		output.createNewFile()
		output.printWriter().use { pw ->
			pw.println("# Values")
			pw.println("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			TablePrinter.toMarkdown(collector).forEach { pw.println(it) }
		}
	}

}
