package io.github.thomo.valuestable.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.*

private const val DEFAULT_TARGET_MARKDOWN = "build/valuesTable/overview.md"
private const val DEFAULT_TARGET_HTML = "build/valuesTable/overview.html"

class ValuesTablePluginFunctionalTest {
	@field:TempDir
	lateinit var tempFolder: File

	private fun getProjectDir() = tempFolder
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

	private fun runGradle(arg: String) = GradleRunner
		.create()
		.forwardOutput()
		.withPluginClasspath()
		.withArguments(arg)
		.withProjectDir(getProjectDir())
		.build()

	@BeforeEach
	fun setUp() {
		getSettingsFile().writeText("")
		getBuildFile().writeText(BuildFileGenerator().build())

		createDefaultValueFile(File(getProjectDir(), "testdata/values.yaml"))
		createValuesFile(File(getProjectDir(), "testdata/values-dev.yaml"), "bDev", "")
		createValuesFile(File(getProjectDir(), "testdata/values-test.yaml"), "bTest", "cTest")
	}

	@Nested
	inner class TaskAvailability {

		@Test
		fun `can run task`() {
			val result = runGradle("valuesTable")
			assertTrue(result.output.contains("Overview generated at"))
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
			val result = runGradle("tasks")

			assertThat(
				result.output.split('\n'),
				hasItem("valuesTable - Creates an overview of helm values")
			)
		}

	}

	@Nested
	inner class GenerateMarkdown {
		@Test
		fun `generate table header`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])

			assertThat(lines, hasItem("""|key|default|dev|test|"""))
		}

		@Test
		fun `generate value line of key root-a`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])
			assertThat(lines, hasItem("""|root.a|"aaa"|*default*|*default*|"""))
		}

		@Test
		fun `generate value line of key root-b`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])
			assertThat(lines, hasItem("""|root.b|*(n.d.)*|"bDev"|"bTest"|"""))
		}

	}

	@Nested
	inner class TaskConfig {
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
			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))

			assertTrue(File(tempFolder, "testdata/anotheroverview.md").exists())
		}

		@Test
		fun `should generate table in html format`() {
			getBuildFile().writeText(BuildFileGenerator().format("html").build())
			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))

			assertTrue(File(tempFolder, DEFAULT_TARGET_HTML).exists())
		}
	}

	@Nested
	inner class TaskBehavior {
		@Test
		fun `should be up to date when running without changes`() {
			val result = runGradle("valuesTable")
			val resultUpToDate = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertThat(resultUpToDate.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.UP_TO_DATE))
		}

		@Test
		fun `should regenerate target when target was removed`() {
			runGradle("valuesTable")

			File(tempFolder, DEFAULT_TARGET_MARKDOWN).delete()
			assertFalse(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())

			runGradle("valuesTable")

			assertTrue(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())
		}

		@Ignore("not yet implemented")
		@Test
		fun `should update target when one input file was changed`() {
			runGradle("valuesTable")

			createValuesFile(File(getProjectDir(), "testdata/values-test.yaml"), "bTest", "xTest")

			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			assertThat(lines, hasItem("""|root.c|*ccc*|null|"xTest"|"""))
		}

	}
}
