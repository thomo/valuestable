package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

// Takes `ObjectFactory` rather than `Project`: this type is instantiated via the `charts`
// container's default reflective factory (see `ValuesTableExtension.charts`), and a `Project`
// reference reachable from a registered chart spec is not serializable by the configuration cache.
open class ValuesChartSpec @Inject constructor(private val name: String, objects: ObjectFactory) : Named {
	override fun getName(): String = name

	val files: NamedDomainObjectContainer<NamedFile> = objects.domainObjectContainer(NamedFile::class.java).also { container ->
		container.whenObjectAdded { file ->
			fileOrder.add(file.name)
		}
	}

	private val fileOrder = mutableListOf<String>()

	fun getFilesInOrder(): List<NamedFile> {
		return fileOrder.mapNotNull { name -> files.findByName(name) }
	}
}
