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

		// Read envs from project property if provided
		if (project.hasProperty("envs")) {
			ext.envs.set(project.property("envs") as String)
		}

		val task = project.tasks.register(TASK_NAME, ValuesTableTask::class.java).get().apply {
			this.group = TASK_GROUP
			this.description = TASK_DESCRIPTION
		}

		task.format.set(ext.format)
		task.target.set(ext.target)
		task.envs.set(ext.envs)
		task.sources.set(project.provider { ext.getFilesInOrder() })
		task.outputMarkdown.set(task.target.map { path -> project.layout.projectDirectory.file("$path.md") })
		task.outputHtml.set(task.target.map { path -> project.layout.projectDirectory.file("$path.html") })
	}
}
