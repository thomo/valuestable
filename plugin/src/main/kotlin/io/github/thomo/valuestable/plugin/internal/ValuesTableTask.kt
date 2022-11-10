package io.github.thomo.valuestable.plugin.internal

import io.github.thomo.valuestable.TablePrinter
import io.github.thomo.valuestable.ValueCollector
import io.github.thomo.valuestable.ValueReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
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
		output = getTargetFile(extension.target).absoluteFile

		createOverviewFile(extension.files.toList(), output)

		println("Overview generated at ${output.absoluteFile}")
	}

	private fun getTargetFile(target: String): File {
		if (target.isBlank()) return output

		val file = File(target)
		return if (file.isAbsolute) file else File(project.projectDir, target)
	}

	private fun createOverviewFile(sources: List<NamedFile>, target: File) {
		val reader = ValueReader()
		val collector = ValueCollector().apply {
			sources.forEach { src ->
				this.add(src.name, reader.read(project.projectDir.toPath().resolve(src.file)))
			}
		}

		target.createNewFile()
		target.printWriter().use { pw ->
			pw.println("# Values")
			pw.println("")
			pw.println("generated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			pw.println("")
			TablePrinter.toMarkdown(collector).forEach { pw.println(it) }
		}
	}

}
