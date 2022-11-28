package io.github.thomo.valuestable.plugin

import io.github.thomo.valuestable.plugin.constants.TASK_DESCRIPTION
import io.github.thomo.valuestable.plugin.constants.TASK_GROUP
import io.github.thomo.valuestable.plugin.constants.TASK_NAME
import io.github.thomo.valuestable.plugin.internal.ValuesTableExtension
import io.github.thomo.valuestable.plugin.internal.ValuesTableTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ValuesTablePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val ext = project.extensions.create(TASK_NAME, ValuesTableExtension::class.java)

		val task = project.tasks.register(TASK_NAME, ValuesTableTask::class.java).get().apply {
			this.group = TASK_GROUP
			this.description = TASK_DESCRIPTION
		}

		task.format.set(ext.format)
		task.target.set(ext.target)
		task.output.set(task.target.map { path -> project.layout.projectDirectory.file(path) })
	}
}
