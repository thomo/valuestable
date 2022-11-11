package io.github.thomo.valuestable.plugin.internal

import io.github.thomo.valuestable.TablePrinter
import io.github.thomo.valuestable.ValueCollector
import io.github.thomo.valuestable.ValueReader
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ValuesTableTask : DefaultTask() {

	@Input
	val target: Property<String> = project.objects.property(String::class.java)

	@OutputFile
	val output: RegularFileProperty = project.objects.fileProperty()

	@TaskAction
	fun action() {
		val extension = project.extensions.run {
			findByName("valuesTable") as ValuesTableExtension
		}

		createOverviewFile(extension.files.toList(), output.get().asFile)

		println("Overview generated at ${output.get().asFile}")
	}

	private fun createOverviewFile(sources: List<NamedFile>, output: File) {
		val reader = ValueReader()
		val collector = ValueCollector().apply {
			sources.forEach { src ->
				this.add(src.name, reader.read(project.projectDir.toPath().resolve(src.file)))
			}
		}

		output.createNewFile()
		output.printWriter().use { pw -> TablePrinter.toMarkdown(collector).forEach { pw.println(it) } }
	}

}
