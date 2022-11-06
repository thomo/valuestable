package io.github.thomo.valuestable.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import io.github.thomo.valuestable.plugin.internal.ValuesTableExtension
import io.github.thomo.valuestable.plugin.internal.ValuesTableTask

class ValuesTablePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.extensions.create("valuesTable", ValuesTableExtension::class.java)

		project.tasks.create("valuesTable", ValuesTableTask::class.java)
	}

}