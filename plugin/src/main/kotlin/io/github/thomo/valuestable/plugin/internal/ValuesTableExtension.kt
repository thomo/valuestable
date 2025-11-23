package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class ValuesTableExtension(project: Project) {
	val format: Property<String> = project.objects.property(String::class.java).convention("markdown")

	val target: Property<String> = project.objects.property(String::class.java).convention(
		project.layout.buildDirectory
			.file("valuesTable/overview").get()
			.asFile
			.path
	)

	val vtEnvs: Property<String> = project.objects.property(String::class.java).convention("")

	val vtPath: Property<String> = project.objects.property(String::class.java).convention("")

	val files: NamedDomainObjectContainer<NamedFile> = project.objects.domainObjectContainer(NamedFile::class.java).also { container ->
		container.whenObjectAdded { file ->
			fileOrder.add(file.name)
		}
	}
	
	private val fileOrder = mutableListOf<String>()
	
	fun getFilesInOrder(): List<NamedFile> {
		return fileOrder.mapNotNull { name -> files.findByName(name) }
	}
}
