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

private const val VALUES_DEFAULT_FILENAME = "testdata/values.yaml"
private const val VALUES_DEV_FILENAME = "testdata/values-dev.yaml"
private const val VALUES_TEST_FILENAME = "testdata/values-test.yaml"

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

		createDefaultValueFile(File(getProjectDir(), VALUES_DEFAULT_FILENAME))
		createValuesFile(File(getProjectDir(), VALUES_DEV_FILENAME), "bDev", "")
		createValuesFile(File(getProjectDir(), VALUES_TEST_FILENAME), "bTest", "cTest")
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

			assertThat(lines, hasItem("""|key|values|"""))
		}

		@Test
		fun `generate value line of key root-a`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])
			assertThat(lines, hasItem("""|root.a|default: "aaa"<br/>dev: *default*<br/>test: *default*|"""))
		}

		@Test
		fun `generate value line of key root-c`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])
			assertThat(lines, hasItem("""|root.c|default: "ccc"<br/>dev: null<br/>test: "cTest"|"""))
		}

	}

	@Nested
	inner class GenerateHtml {
		@BeforeEach
		internal fun setUp() {
			getBuildFile().writeText(BuildFileGenerator().build())
		}

		@Test
		fun `generate table header`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_HTML).readLines()

			assertThat(lines, hasItem("""<thead><tr><th>key</th><th>values</th></tr></thead>"""))
		}

		@Test
		fun `generate value line of key root-a`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_HTML).readLines()
			assertThat(
				lines,
				hasItem(
					"""<tr><td>root.a</td><td>default: "aaa"<br/>dev: <i>default</i><br/>test: <i>default</i></td></tr>"""
				)
			)
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
				
				target = "testdata/anotheroverview"
				
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
			getBuildFile().writeText(BuildFileGenerator().build())
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

		// @Ignore("not working at the moment")
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

			// first create output with last values is "cTest"
			runGradle("valuesTable")

			// change the file -> should trigger regeneration
			createValuesFile(File(getProjectDir(), VALUES_TEST_FILENAME), "bTest", "xTest")

			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines().filter { it.startsWith("|root.c|") }
			assertThat(lines, hasItem("""|root.c|default: "ccc"<br/>dev: null<br/>test: "xTest"|"""))
		}

	}
}
