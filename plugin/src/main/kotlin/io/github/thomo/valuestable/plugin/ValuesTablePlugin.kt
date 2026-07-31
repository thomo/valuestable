package io.github.thomo.valuestable.plugin

import io.github.thomo.valuestable.plugin.constants.TASK_DESCRIPTION
import io.github.thomo.valuestable.plugin.constants.TASK_GROUP
import io.github.thomo.valuestable.plugin.constants.TASK_NAME
import io.github.thomo.valuestable.plugin.internal.ChartFiles
import io.github.thomo.valuestable.plugin.internal.ValuesTableExtension
import io.github.thomo.valuestable.plugin.internal.ValuesTableTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ValuesTablePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val ext = project.extensions.create(TASK_NAME, ValuesTableExtension::class.java)

		// Read envs from project property if provided
		if (project.hasProperty("vtEnvs")) {
			ext.vtEnvs.set(project.property("vtEnvs") as String)
		}

		// Read path from project property if provided
		if (project.hasProperty("vtPath")) {
			ext.vtPath.set(project.property("vtPath") as String)
		}

		// Read chart selection from project property if provided
		if (project.hasProperty("vtCharts")) {
			ext.vtCharts.set(project.property("vtCharts") as String)
		}

		val task = project.tasks.register(TASK_NAME, ValuesTableTask::class.java).get().apply {
			this.group = TASK_GROUP
			this.description = TASK_DESCRIPTION
		}

		// Default when `target` isn't set: a file basename, so the flat/default table lands at
		// e.g. build/valuesTable/overview.md.
		val defaultFlatTarget = project.layout.buildDirectory.file("valuesTable/overview").map { it.asFile.path }

		task.format.set(ext.format)
		task.target.set(ext.target.orElse(defaultFlatTarget))
		task.vtEnvs.set(ext.vtEnvs)
		task.vtPath.set(ext.vtPath)
		task.wrap.set(ext.wrap)
		task.sources.set(project.provider { ext.getFilesInOrder() })
		// Only populated when `charts { }` + `mergeCharts` are both used; drives the merged
		// single-report path in ValuesTableTask instead of the flat `sources` list. Respects
		// `-PvtCharts` the same way the per-chart sub-tasks below do.
		task.chartSources.set(project.provider {
			if (ext.mergeCharts.getOrElse(false)) {
				ext.getSelectedChartsInOrder(ext.vtCharts.getOrElse(""))
					.map { spec -> ChartFiles(spec.name, spec.getFilesInOrder()) }
			} else {
				emptyList()
			}
		})
		task.outputMarkdown.set(task.target.map { path -> project.layout.projectDirectory.file("$path.md") })
		task.outputHtml.set(task.target.map { path -> project.layout.projectDirectory.file("$path.html") })

		// When `charts { }` is used without `mergeCharts`, the aggregate `valuesTable` task only
		// drives its chart sub-tasks; it must not also run its own flat-mode action against zero
		// sources, which would otherwise produce an empty, unwanted overview.md/.html alongside
		// the real reports. With `mergeCharts` it runs its own action to produce the merged report.
		task.onlyIf("no charts configured, or charts merged into a single report") {
			ext.charts.isEmpty() || ext.mergeCharts.getOrElse(false)
		}

		// Default when `target` isn't set: a folder, so charts land at e.g.
		// build/valuesTable/serviceA.md rather than nested under an "overview" segment meant for
		// the flat-mode file basename above.
		val defaultChartsFolder = project.layout.buildDirectory.dir("valuesTable").map { it.asFile.path }

		// `charts { }` entries are populated after `apply()` runs, while the build script is
		// evaluated, so register sub-tasks lazily via `all { }` rather than iterating eagerly here.
		ext.charts.all { spec ->
			val subTaskName = TASK_NAME + spec.name.replaceFirstChar { it.uppercaseChar() }
			val subTask = project.tasks.register(subTaskName, ValuesTableTask::class.java).get().apply {
				this.group = TASK_GROUP
				this.description = "$TASK_DESCRIPTION (${spec.name})"
			}

			// The shared `target` is the output folder for chart reports; each chart's files
			// are named after its registration name within that folder.
			val chartTarget = ext.target.orElse(defaultChartsFolder).map { folder -> "$folder/${spec.name}" }

			subTask.format.set(ext.format)
			subTask.target.set(chartTarget)
			subTask.vtEnvs.set(ext.vtEnvs)
			subTask.vtPath.set(ext.vtPath)
			subTask.wrap.set(ext.wrap)
			subTask.reportName.set(spec.name)
			subTask.sources.set(project.provider { spec.getFilesInOrder() })
			subTask.outputMarkdown.set(subTask.target.map { path -> project.layout.projectDirectory.file("$path.md") })
			subTask.outputHtml.set(subTask.target.map { path -> project.layout.projectDirectory.file("$path.html") })

			// With `mergeCharts`, the aggregate task renders the merged report itself and must
			// not also pull in (and run) every per-chart sub-task. Otherwise, only pull in
			// sub-tasks for charts selected via `-PvtCharts` (all of them when unset).
			task.dependsOn(project.provider {
				val charts = ext.vtCharts.getOrElse("")
				if (ext.mergeCharts.getOrElse(false) || ext.getSelectedChartsInOrder(charts).none { it.name == spec.name }) {
					emptyList()
				} else {
					listOf(subTask)
				}
			})
		}

		// Both containers are fully populated only once the build script has finished evaluating,
		// regardless of which block (`files { }` vs `charts { }`) was written first.
		project.afterEvaluate {
			if (ext.files.isNotEmpty() && ext.charts.isNotEmpty()) {
				throw GradleException(
					"valuesTable: configure sources via either the top-level `files { }` block or the " +
						"`charts { }` block, not both. Move existing `files { }` entries into a named " +
						"entry under `charts { }`, or remove `charts { }` to keep using the top-level `files { }`."
				)
			}
		}
	}
}
