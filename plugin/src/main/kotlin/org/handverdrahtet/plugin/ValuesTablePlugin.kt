package org.handverdrahtet.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.handverdrahtet.plugin.internal.ValuesTableExtension
import org.handverdrahtet.plugin.internal.ValuesTableTask

class ValuesTablePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.extensions.create("valuesTable", ValuesTableExtension::class.java)

		project.tasks.create("valuesTable", ValuesTableTask::class.java)
	}

}
