package io.github.thomo.valuestable.plugin.internal

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class ValuesTableExtension @Inject constructor(objects: ObjectFactory) {
	var files: NamedDomainObjectContainer<NamedFile> = objects.domainObjectContainer(NamedFile::class.java)
}
