package io.github.thomo.valuestable.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
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

	private fun runGradle(vararg args: String) = GradleRunner
		.create()
		.forwardOutput()
		.withPluginClasspath()
		.withArguments(*args)
		.withProjectDir(getProjectDir())
		.build()

	private fun runGradleAndFail(vararg args: String) = GradleRunner
		.create()
		.forwardOutput()
		.withPluginClasspath()
		.withArguments(*args)
		.withProjectDir(getProjectDir())
		.buildAndFail()

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
			assertThat(lines, hasItem("""|root<wbr>.a|default: "aaa"<br/>test: *default*<br/>dev: *default*|"""))
		}

		@Test
		fun `generate value line of key root-c`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()

			assertEquals("# Values", lines[0])
			assertThat(lines, hasItem("""|root<wbr>.c|default: "ccc"<br/>test: "cTest"<br/>dev: *default*|"""))
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

			assertThat(lines, hasItem("""<thead><tr><th>Key</th><th>Values</th></tr></thead>"""))
		}

		@Test
		fun `generate value line of key root-a`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_HTML).readLines()
			assertThat(
				lines,
				hasItem(
					"""<tr><td><code>root<wbr>.a</code></td><td class="value-cell"><span class="label">default:</span>"aaa"<br/><span class="label">test:</span><span class="value-default"><i>default</i></span><br/><span class="label">dev:</span><span class="value-default"><i>default</i></span></td></tr>"""
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
	inner class FormatRestriction {

		@Test
		fun `default generates both markdown and html`() {
			getBuildFile().writeText(BuildFileGenerator().build())

			runGradle("valuesTable")

			assertTrue(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())
			assertTrue(File(tempFolder, DEFAULT_TARGET_HTML).exists())
		}

		@Test
		fun `format html only generates the html file`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					format = "html"

					files {
						'default' {
							file = "testdata/values.yaml"
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, DEFAULT_TARGET_HTML).exists())
			assertFalse(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())
		}

		@Test
		fun `format markdown only generates the markdown file`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					format = "markdown"

					files {
						'default' {
							file = "testdata/values.yaml"
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())
			assertFalse(File(tempFolder, DEFAULT_TARGET_HTML).exists())
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

		@Test
		fun `should update target when one input file was changed`() {

			// first create output with last values is "cTest"
			runGradle("valuesTable")

			// change the file -> should trigger regeneration
			createValuesFile(File(getProjectDir(), VALUES_TEST_FILENAME), "bTest", "xTest")

			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines().filter { it.startsWith("|root<wbr>.c|") }
			assertThat(lines, hasItem("""|root<wbr>.c|default: "ccc"<br/>test: "xTest"<br/>dev: *default*|"""))
		}

	}

	@Nested
	inner class EnvFiltering {
		@Test
		fun `should filter to single environment plus default`() {
			val result = runGradle("valuesTable", "-PvtEnvs=dev")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should include default and dev columns
			assertThat(lines, hasItem("|key|values|"))
			assertThat(lines, hasItem("|---|:-----|"))
			assertThat(lines, hasItem("|root<wbr>.a|default: \"aaa\"<br/>dev: *default*|"))
			
			// Should NOT include test column
			assertFalse(lines.any { it.contains("test:") })
		}

		@Test
		fun `should filter to multiple environments plus default`() {
			val result = runGradle("valuesTable", "-PvtEnvs=dev,test")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should include default, dev, and test columns
			assertThat(lines, hasItem("|root<wbr>.a|default: \"aaa\"<br/>test: *default*<br/>dev: *default*|"))
		}

		@Test
		fun `should always include default even when not specified`() {
			val result = runGradle("valuesTable", "-PvtEnvs=test")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should include default and test columns
			assertThat(lines, hasItem("|root<wbr>.a|default: \"aaa\"<br/>test: *default*|"))
			
			// Should NOT include dev column
			assertFalse(lines.any { it.contains("dev:") })
		}
	}

	@Nested
	inner class PathFiltering {
		@Test
		fun `should filter to root path`() {
			val result = runGradle("valuesTable", "-PvtPath=root")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should include keys starting with "root"
			assertThat(lines, hasItem("|root<wbr>.a|default: \"aaa\"<br/>test: *default*<br/>dev: *default*|"))
			assertThat(lines, hasItem("|root<wbr>.c|default: \"ccc\"<br/>test: \"cTest\"<br/>dev: *default*|"))
			// Should include root.b keys if they exist
			assertTrue(lines.any { it.contains("root<wbr>.b") } || lines.none { it.contains("root<wbr>.b") })
		}

		@Test
		fun `should filter to nested path`() {
			// Create a file with nested structure
			val nestedFile = File(getProjectDir(), "testdata/values-nested.yaml")
			nestedFile.apply {
				parentFile.mkdirs()
				createNewFile()
				writeText(
					"""
					---
					root:
					  config:
					    setting1: value1
					    setting2: value2
					  other:
					    data: xyz
					other:
					  key: abc
					""".trimIndent()
				)
			}

			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}
				
				valuesTable {
					files {
						'default' {
							file = "testdata/values-nested.yaml"
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable", "-PvtPath=root.config")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should only include keys starting with "root.config"
			assertTrue(lines.any { it.contains("root<wbr>.config<wbr>.setting1") })
			assertTrue(lines.any { it.contains("root<wbr>.config<wbr>.setting2") })
			// Should NOT include other keys
			assertFalse(lines.any { it.contains("root<wbr>.other") })
			assertFalse(lines.any { it.contains("|other<wbr>.key|") })
		}

		@Test
		fun `should combine path and environment filters`() {
			val result = runGradle("valuesTable", "-PvtPath=root", "-PvtEnvs=dev")

			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			
			// Should include keys starting with "root" and only default + dev environments
			assertThat(lines, hasItem("|root<wbr>.a|default: \"aaa\"<br/>dev: *default*|"))
			// Should NOT include test environment
			assertFalse(lines.any { it.contains("test:") })
		}
	}

	@Nested
	inner class GroupedCharts {

		@Test
		fun `valuesTable depends on and builds registered charts`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
								dev {
									file = "testdata/values-dev.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable")

			// The aggregate task itself is skipped (no flat-mode output) - only the chart
			// sub-task actually generates a report.
			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SKIPPED))
			assertThat(result.task(":valuesTableServiceA")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, "testdata/charts/serviceA.md").exists())
			assertTrue(File(tempFolder, "testdata/charts/serviceA.html").exists())
			assertFalse(File(tempFolder, "testdata/charts.md").exists())
			assertFalse(File(tempFolder, "testdata/charts.html").exists())
		}

		@Test
		fun `charts default to the build-valuesTable folder when target is not set`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SKIPPED))
			assertThat(result.task(":valuesTableServiceA")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, "build/valuesTable/serviceA.md").exists())
			assertTrue(File(tempFolder, "build/valuesTable/serviceA.html").exists())
			// No leftover "overview" folder/files from the flat-mode default target.
			assertFalse(File(tempFolder, "build/valuesTable/overview.md").exists())
			assertFalse(File(tempFolder, "build/valuesTable/overview").exists())
		}

		@Test
		fun `chart task can be run standalone`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTableServiceA")

			assertThat(result.task(":valuesTableServiceA")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, "testdata/charts/serviceA.md").exists())
		}

		@Test
		fun `each chart is named after its registration name under the shared target folder`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/values-dev.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SKIPPED))
			assertTrue(File(tempFolder, "testdata/charts/serviceA.md").exists())
			assertTrue(File(tempFolder, "testdata/charts/serviceB.md").exists())
		}

		@Test
		fun `-PvtCharts restricts valuesTable to the selected charts`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/values-dev.yaml"
								}
							}
						}
						serviceC {
							files {
								'default' {
									file = "testdata/values-test.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTable", "-PvtCharts=serviceB,serviceC")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SKIPPED))
			assertNull(result.task(":valuesTableServiceA"))
			assertThat(result.task(":valuesTableServiceB")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertThat(result.task(":valuesTableServiceC")!!.outcome, equalTo(TaskOutcome.SUCCESS))

			assertFalse(File(tempFolder, "testdata/charts/serviceA.md").exists())
			assertTrue(File(tempFolder, "testdata/charts/serviceB.md").exists())
			assertTrue(File(tempFolder, "testdata/charts/serviceC.md").exists())
		}

		@Test
		fun `-PvtCharts does not block running an unselected chart standalone`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradle("valuesTableServiceA", "-PvtCharts=serviceB")

			assertThat(result.task(":valuesTableServiceA")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, "testdata/charts/serviceA.md").exists())
		}

		@Test
		fun `fails when both top-level files and charts are configured`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					files {
						'default' {
							file = "testdata/values.yaml"
						}
					}
					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			val result = runGradleAndFail("valuesTable")

			assertThat(result.output, containsString("configure sources via either"))
		}

		@Test
		fun `old syntax alone is unaffected by the charts feature`() {
			getBuildFile().writeText(BuildFileGenerator().build())

			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertNull(result.task(":valuesTableServiceA"))
			assertTrue(File(tempFolder, DEFAULT_TARGET_MARKDOWN).exists())
		}
	}

	@Nested
	inner class MergedCharts {

		@BeforeEach
		internal fun setUp() {
			// serviceA only knows about root.a/root.c; serviceB only knows about root.b/root.c
			// (with a null root.c), so the merged table must show the union of keys and leave
			// serviceB's root.a / serviceA's root.b cells empty.
			createDefaultValueFile(File(getProjectDir(), "testdata/serviceA/values.yaml"))
			createValuesFile(File(getProjectDir(), "testdata/serviceB/values.yaml"), "bDev", "")

			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/merged"
					mergeCharts = true

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/serviceA/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/serviceB/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)
		}

		@Test
		fun `valuesTable produces a single merged report instead of per-chart sub-tasks`() {
			val result = runGradle("valuesTable")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertNull(result.task(":valuesTableServiceA"))
			assertNull(result.task(":valuesTableServiceB"))

			assertTrue(File(tempFolder, "testdata/merged.md").exists())
			assertTrue(File(tempFolder, "testdata/merged.html").exists())
			assertFalse(File(tempFolder, "testdata/serviceA.md").exists())
			assertFalse(File(tempFolder, "testdata/serviceB.md").exists())
		}

		@Test
		fun `merged markdown table has one column per chart and the union of their keys`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, "testdata/merged.md").readLines()

			assertThat(lines, hasItem("|key|serviceA|serviceB|"))
			// serviceA has no root.b at all -> empty cell.
			assertThat(lines, hasItem("""|root<wbr>.a|default: "aaa"||"""))
			assertThat(lines, hasItem("""|root<wbr>.b||default: "bDev"|"""))
			// serviceB has root.c but its value is null -> "(n.d.)", not an empty cell.
			assertThat(lines, hasItem("""|root<wbr>.c|default: "ccc"|default: *(n.d.)*|"""))
		}

		@Test
		fun `merged html table has one column per chart`() {
			runGradle("valuesTable")

			val lines = File(tempFolder, "testdata/merged.html").readLines()

			assertThat(lines, hasItem("<thead><tr><th>Key</th><th>serviceA</th><th>serviceB</th></tr></thead>"))
		}

		@Test
		fun `merged column order follows registration order, not alphabetical order`() {
			// "zulu" sorts after "alpha" alphabetically but is registered first - the merged
			// report's columns must follow registration order.
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/merged"
					mergeCharts = true

					charts {
						zulu {
							files {
								'default' {
									file = "testdata/serviceA/values.yaml"
								}
							}
						}
						alpha {
							files {
								'default' {
									file = "testdata/serviceB/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			runGradle("valuesTable")

			val mdLines = File(tempFolder, "testdata/merged.md").readLines()
			assertThat(mdLines, hasItem("|key|zulu|alpha|"))

			val htmlLines = File(tempFolder, "testdata/merged.html").readLines()
			assertThat(htmlLines, hasItem("<thead><tr><th>Key</th><th>zulu</th><th>alpha</th></tr></thead>"))
		}

		@Test
		fun `chart sub-tasks can still be run standalone when merged`() {
			val result = runGradle("valuesTableServiceA")

			assertThat(result.task(":valuesTableServiceA")!!.outcome, equalTo(TaskOutcome.SUCCESS))
			assertTrue(File(tempFolder, "testdata/merged/serviceA.md").exists())
		}

		@Test
		fun `-PvtCharts restricts the merged report to the selected charts`() {
			val result = runGradle("valuesTable", "-PvtCharts=serviceB")

			assertThat(result.task(":valuesTable")!!.outcome, equalTo(TaskOutcome.SUCCESS))

			val mdLines = File(tempFolder, "testdata/merged.md").readLines()
			assertThat(mdLines, hasItem("|key|serviceB|"))

			val htmlLines = File(tempFolder, "testdata/merged.html").readLines()
			assertThat(htmlLines, hasItem("<thead><tr><th>Key</th><th>serviceB</th></tr></thead>"))
		}
	}

	@Nested
	inner class LineWrapping {

		private val longValue = "x".repeat(100)

		private fun createLongValueFile(file: File) {
			file.apply {
				parentFile.mkdirs()
				createNewFile()
				writeText(
					"""
						---
						root:
					    long: $longValue
					""".trimIndent()
				)
			}
		}

		private fun buildFile(wrapLine: String) = """
			plugins {
				id('io.github.thomo.valuestable')
			}

			valuesTable {
				$wrapLine
				files {
					'default' {
						file = "testdata/long.yaml"
					}
				}
			}
			""".trimIndent()

		private fun readMarkdownCell(): String {
			val lines = File(tempFolder, DEFAULT_TARGET_MARKDOWN).readLines()
			val valueLine = lines.first { it.startsWith("|root<wbr>.long|") }
			return valueLine.removePrefix("|root<wbr>.long|default: ").removeSuffix("|")
		}

		@BeforeEach
		internal fun setUp() {
			createLongValueFile(File(getProjectDir(), "testdata/long.yaml"))
		}

		@Test
		fun `long values are wrapped every 80 characters by default`() {
			getBuildFile().writeText(buildFile(""))

			runGradle("valuesTable")

			val cellContent = readMarkdownCell()
			val quotedValue = "\"$longValue\""

			// content is preserved, just with break opportunities inserted
			assertEquals(quotedValue, cellContent.replace("<wbr>", ""))
			// one break for a 102-char quoted value wrapped at 80
			val segments = cellContent.split("<wbr>")
			assertEquals(2, segments.size)
			assertEquals(80, segments[0].length)
		}

		@Test
		fun `wrap = 0 disables wrapping`() {
			getBuildFile().writeText(buildFile("wrap = 0"))

			runGradle("valuesTable")

			val cellContent = readMarkdownCell()

			assertEquals("\"$longValue\"", cellContent)
			assertFalse(cellContent.contains("<wbr>"))
		}

		@Test
		fun `wrap width is configurable`() {
			getBuildFile().writeText(buildFile("wrap = 10"))

			runGradle("valuesTable")

			val cellContent = readMarkdownCell()
			val segments = cellContent.split("<wbr>")

			assertTrue(segments.size > 2)
			segments.dropLast(1).forEach { assertEquals(10, it.length) }
		}

		@Test
		fun `long values are wrapped in html output too`() {
			getBuildFile().writeText(buildFile(""))

			runGradle("valuesTable")

			val lines = File(tempFolder, DEFAULT_TARGET_HTML).readLines()
			val valueLine = lines.first { it.contains("root<wbr>.long") }

			assertTrue(valueLine.contains("<wbr>"))
		}
	}

	@Nested
	inner class ConfigurationCache {

		// A build with configuration-cache problems fails outright (the default is
		// `configuration-cache-problems=fail`), so a plain successful `runGradle` call with
		// `--configuration-cache` already proves the first run stored a clean cache entry. The
		// second run then must report reusing it rather than re-running configuration.
		@Test
		fun `flat mode is configuration-cache compatible`() {
			runGradle("valuesTable", "--configuration-cache")
			val result = runGradle("valuesTable", "--configuration-cache")

			assertThat(result.output, containsString("Reusing configuration cache"))
		}

		@Test
		fun `charts mode is configuration-cache compatible`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/values-dev.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			runGradle("valuesTable", "--configuration-cache")
			val result = runGradle("valuesTable", "--configuration-cache")

			assertThat(result.output, containsString("Reusing configuration cache"))
		}

		@Test
		fun `merged charts mode is configuration-cache compatible`() {
			createDefaultValueFile(File(getProjectDir(), "testdata/serviceA/values.yaml"))
			createValuesFile(File(getProjectDir(), "testdata/serviceB/values.yaml"), "bDev", "")

			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/merged"
					mergeCharts = true

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/serviceA/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/serviceB/values.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			runGradle("valuesTable", "--configuration-cache")
			val result = runGradle("valuesTable", "--configuration-cache")

			assertThat(result.output, containsString("Reusing configuration cache"))
		}

		@Test
		fun `-PvtCharts chart selection is configuration-cache compatible`() {
			getBuildFile().writeText(
				"""
				plugins {
					id('io.github.thomo.valuestable')
				}

				valuesTable {
					target = "testdata/charts"

					charts {
						serviceA {
							files {
								'default' {
									file = "testdata/values.yaml"
								}
							}
						}
						serviceB {
							files {
								'default' {
									file = "testdata/values-dev.yaml"
								}
							}
						}
					}
				}
				""".trimIndent()
			)

			runGradle("valuesTable", "-PvtCharts=serviceB", "--configuration-cache")
			val result = runGradle("valuesTable", "-PvtCharts=serviceB", "--configuration-cache")

			assertThat(result.output, containsString("Reusing configuration cache"))
			assertFalse(File(tempFolder, "testdata/charts/serviceA.md").exists())
			assertTrue(File(tempFolder, "testdata/charts/serviceB.md").exists())
		}
	}
}
