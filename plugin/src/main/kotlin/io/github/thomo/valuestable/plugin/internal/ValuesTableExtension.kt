package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class ValuesTableExtension(project: Project) {
	val target: Property<String> = project.objects.property(String::class.java).convention(
		project.layout.buildDirectory
			.file("valuesTable/overview.md").get()
			.asFile
			.path
	)

	val files: NamedDomainObjectContainer<NamedFile> = project.objects.domainObjectContainer(NamedFile::class.java)
}
