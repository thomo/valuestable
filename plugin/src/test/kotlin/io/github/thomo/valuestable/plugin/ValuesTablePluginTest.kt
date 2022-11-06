package io.github.thomo.valuestable.plugin

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class ValuesTablePluginTest {
	@Test
	fun `plugin registers task`() {
		val project = ProjectBuilder.builder().build()
		project.plugins.apply("io.github.thomo.valuestable")

		assertNotNull(project.tasks.findByName("valuesTable"))
	}
}
