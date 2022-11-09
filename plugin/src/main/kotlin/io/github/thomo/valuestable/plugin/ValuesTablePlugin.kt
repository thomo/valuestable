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
		project.extensions.create(TASK_NAME, ValuesTableExtension::class.java)

		project.tasks.create(TASK_NAME, ValuesTableTask::class.java).apply {
			this.description = TASK_DESCRIPTION
			this.group = TASK_GROUP
		}
	}

}
