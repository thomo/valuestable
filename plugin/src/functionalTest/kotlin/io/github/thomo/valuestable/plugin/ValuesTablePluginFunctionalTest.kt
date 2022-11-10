package io.github.thomo.valuestable.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val BUILD_WITH_PARAMS = """
plugins {
    id('io.github.thomo.valuestable')
}

valuesTable {
	files {
		'default' {
			file = "testdata/values.yaml"
		}
		test {
			file = "testdata/values-test.yaml"
		}
		dev {
			file = "testdata/values-dev.yaml"
		}
	}
}
"""

class ValuesTablePluginFunctionalTest {
	@get:Rule
	val tempFolder = TemporaryFolder()

	private fun getProjectDir() = tempFolder.root
	private fun getBuildFile() = getProjectDir().resolve("build.gradle")
	private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")

	private fun createDefaultValueFile(file: File) {
		file.apply {
			parentFile.mkdirs()
			createNewFile()
			writeText(
				"""
					---
					root:
				    a: aaa
				    c: ccc
					""".trimIndent()
			)
		}
	}

	private fun createValuesFile(file: File, b: String, c: String) {
		file.apply {
			parentFile.mkdirs()
			createNewFile()
			writeText(
				"""
					---
					root:
				""".trimIndent()
			)
			appendText("\n")
			appendText("  b: $b\n")
			appendText("  c: $c\n")
		}
	}

	@Before
	fun setUp() {
		getSettingsFile().writeText("")
		getBuildFile().writeText(BUILD_WITH_PARAMS)

		createDefaultValueFile(File(getProjectDir(), "testdata/values.yaml"))
		createValuesFile(File(getProjectDir(), "testdata/values-dev.yaml"), "bDev", "")
		createValuesFile(File(getProjectDir(), "testdata/values-test.yaml"), "bTest", "cTest")
	}

	@Test
	fun `can run task`() {
		val result = GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		// Verify the result
		assertTrue(result.output.contains("Overview generated at"))
	}

	@Test
	fun `generate table header`() {
		GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		val lines = File(tempFolder.root, "build/valuesTable/overview.md").readLines()

		assertEquals("# Values", lines[0])
		assertThat(lines, hasItem("""|key|default|dev|test|"""))
	}

	@Test
	fun `generate value line of key root-a`() {
		GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		val lines = File(tempFolder.root, "build/valuesTable/overview.md").readLines()

		assertEquals("# Values", lines[0])
		assertThat(lines, hasItem("""|root.a|"aaa"|*default*|*default*|"""))
	}

	@Test
	fun `generate value line of key root-b`() {
		GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		val lines = File(tempFolder.root, "build/valuesTable/overview.md").readLines()

		assertEquals("# Values", lines[0])
		assertThat(lines, hasItem("""|root.b|*(n.d.)*|"bDev"|"bTest"|"""))
	}

	@Test
	fun `apply run task twice - should be up to date`() {
		val result = GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		val resultUpToDate = GradleRunner.create()
			.withProjectDir(getProjectDir())
			.withPluginClasspath()
			.withArguments("valuesTable")
			.forwardOutput()
			.build()

		assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
		assertThat(resultUpToDate.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.UP_TO_DATE))
	}

	@Test
	fun `should create target at specified location`() {
		getBuildFile().writeText(
			"""
			plugins {
					id('io.github.thomo.valuestable')
			}
			
			valuesTable {
				target = "testdata/anotheroverview.md"
				files {
					'default' {
						file = "testdata/values.yaml"
					}
					test {
						file = "testdata/values-test.yaml"
					}
					dev {
						file = "testdata/values-dev.yaml"
					}
				}
			}
			""".trimIndent()
		)
		val result = GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("valuesTable")
			.withProjectDir(getProjectDir())
			.build()

		assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))

		assertTrue(File(tempFolder.root, "testdata/anotheroverview.md").exists())

		val lines = File(tempFolder.root, "testdata/anotheroverview.md").readLines()

		assertEquals("# Values", lines[0])
		assertThat(lines, hasItem("""|key|default|dev|test|"""))
	}

	@Test
	fun `should show task description`() {
		getBuildFile().writeText(
			"""
			plugins {
					id('io.github.thomo.valuestable')
			}
			""".trimIndent()
		)
		val result = GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("tasks")
			.withProjectDir(getProjectDir())
			.build()

		assertThat(
			result.output.split('\n'),
			hasItem("valuesTable - Creates an overview of helm values")
		)
	}
}
