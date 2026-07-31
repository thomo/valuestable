package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import javax.inject.Inject

open class ValuesChartSpec @Inject constructor(private val name: String, project: Project) : Named {
	override fun getName(): String = name

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
