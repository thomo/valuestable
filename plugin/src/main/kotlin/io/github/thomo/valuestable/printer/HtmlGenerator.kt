package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HtmlGenerator(private val name: String? = null, private val wrap: Int = 0) : Generator {
	private val title = if (name.isNullOrBlank()) "Values Overview" else "Values Overview '$name'"

	override fun generate(collector: ValueCollector) = pageShell() +
		generateTableHead() + "<tbody>" + generateTableRows(collector) +
		"</tbody></table></body></html>"

	override fun generateMerged(charts: List<Pair<String, ValueCollector>>): List<String> {
		val keySets = charts.map { (_, vc) -> vc.keys().toSet() }
		val globalKeys = keySets.flatten().toSet().sorted()

		return pageShell() +
			generateMergedTableHead(charts) + "<tbody>" + generateMergedTableRows(globalKeys, charts, keySets) +
			"</tbody></table></body></html>"
	}

	private fun pageShell() = mutableListOf(
		"""
			<!DOCTYPE html>
			<html lang="en">
			<head>
			<meta charset="UTF-8">
			<meta name="viewport" content="width=device-width, initial-scale=1.0">
			<title>$title</title>
			<style>
				:root {
					--primary-color: #007bff;
					--bg-color: #f8f9fa;
					--text-color: #333;
					--border-color: #dee2e6;
					--header-bg: #e9ecef;
					--hover-bg: #f1f3f5;
				}
				body {
					font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
					line-height: 1.5;
					color: var(--text-color);
					background-color: var(--bg-color);
					margin: 0;
					padding: 20px;
				}
				.header-container {
					display: flex;
					justify-content: space-between;
					align-items: flex-end;
					border-bottom: 2px solid var(--primary-color);
					padding-bottom: 10px;
					margin-bottom: 20px;
				}
				h1 {
					color: var(--primary-color);
					margin: 0;
					padding: 0;
				}
				.meta {
					color: #6c757d;
					font-size: 0.9em;
					margin: 0;
					text-align: right;
				}
				table {
					width: 100%;
					border-collapse: collapse;
					background-color: white;
					box-shadow: 0 1px 3px rgba(0,0,0,0.1);
				}
				th, td {
					padding: 12px 15px;
					text-align: left;
					border-bottom: 1px solid var(--border-color);
				}
				th {
					background-color: var(--header-bg);
					font-weight: 600;
					position: sticky;
					top: 0;
					z-index: 10;
				}
				tr:hover {
					background-color: var(--hover-bg);
				}
				code {
					font-family: SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
					background-color: #f8f9fa;
					padding: 2px 4px;
					border-radius: 4px;
					color: #e83e8c;
				}
				.value-cell {
					line-height: 1.6;
				}
				.label {
					font-weight: 500;
					color: #495057;
					margin-right: 4px;
				}
				.filter-container {
					margin-bottom: 20px;
					display: flex;
					gap: 15px;
					align-items: center;
				}
				.filter-input {
					flex: 1;
					max-width: 400px;
					padding: 10px 15px;
					border: 1px solid var(--border-color);
					border-radius: 4px;
					font-size: 14px;
					font-family: inherit;
				}
				.filter-input:focus {
					outline: none;
					border-color: var(--primary-color);
					box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
				}
				.row-count {
					color: #6c757d;
					font-size: 0.9em;
				}
				tr.hidden {
					display: none;
				}
				.value-default {
					opacity: 0.5;
					font-style: italic;
				}
				.value-different {
					background-color: #fff3cd;
					padding: 2px 6px;
					border-radius: 3px;
					border-left: 3px solid #ffc107;
				}
			</style>
			<script>
				function filterTable() {
					const input = document.getElementById('filterInput');
					const filter = input.value.toLowerCase();
					const table = document.getElementById('valuesTable');
					const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
					let visibleCount = 0;
					
					for (let i = 0; i < rows.length; i++) {
						const row = rows[i];
						const cells = row.getElementsByTagName('td');
						let found = false;
						
						for (let j = 0; j < cells.length; j++) {
							const cellText = cells[j].textContent || cells[j].innerText;
							if (cellText.toLowerCase().indexOf(filter) > -1) {
								found = true;
								break;
							}
						}
						
						if (found) {
							row.classList.remove('hidden');
							visibleCount++;
						} else {
							row.classList.add('hidden');
						}
					}
					
					document.getElementById('rowCount').textContent = 
						`Showing ${'$'}{visibleCount} of ${'$'}{rows.length} rows`;
				}
				
				window.addEventListener('DOMContentLoaded', function() {
					const totalRows = document.getElementById('valuesTable')
						.getElementsByTagName('tbody')[0]
						.getElementsByTagName('tr').length;
					document.getElementById('rowCount').textContent = 
						`Showing ${'$'}{totalRows} of ${'$'}{totalRows} rows`;
				});
			</script>
			</head>
			<body>
			""".trimIndent(),
		"<div class=\"header-container\">",
		"<h1>$title</h1>",
		"<p class=\"meta\">Generated at " + LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
			" | <a href=\"https://github.com/thomo/valuestable\" target=\"_blank\" rel=\"noopener\">ValuesTable Plugin</a></p>",
		"</div>",
		"<div class=\"filter-container\">",
		"<input type=\"text\" id=\"filterInput\" class=\"filter-input\" placeholder=\"Filter by key or value...\" onkeyup=\"filterTable()\">",
		"<span id=\"rowCount\" class=\"row-count\"></span>",
		"</div>",
		"<table id=\"valuesTable\">"
	)

	fun generateTableHead() =
		"<thead><tr><th>Key</th><th>Values</th></tr></thead>"

	fun generateTableRows(collector: ValueCollector) =
		collector.keys().map { key -> "<tr>" + generateTableRow(key, collector) + "</tr>" }

	fun generateTableRow(key: String, collector: ValueCollector): String {
		val formattedKey = key.replace(".", "<wbr>.")
		return "<td><code>$formattedKey</code></td><td class=\"value-cell\">" + buildValueCellContent(key, collector) + "</td>"
	}

	private fun generateMergedTableHead(charts: List<Pair<String, ValueCollector>>) =
		"<thead><tr><th>Key</th>" + charts.joinToString("") { (name, _) -> "<th>$name</th>" } + "</tr></thead>"

	private fun generateMergedTableRows(
		keys: List<String>,
		charts: List<Pair<String, ValueCollector>>,
		keySets: List<Set<String>>
	) = keys.map { key -> "<tr>" + generateMergedTableRow(key, charts, keySets) + "</tr>" }

	private fun generateMergedTableRow(key: String, charts: List<Pair<String, ValueCollector>>, keySets: List<Set<String>>): String {
		val formattedKey = key.replace(".", "<wbr>.")
		val cells = charts.mapIndexed { index, (_, vc) ->
			val content = if (key in keySets[index]) buildValueCellContent(key, vc) else ""
			"<td class=\"value-cell\">$content</td>"
		}
		return "<td><code>$formattedKey</code></td>" + cells.joinToString("")
	}

	private fun buildValueCellContent(key: String, collector: ValueCollector): String {
		val names = collector.getNames()
		val values = collector.getValues(key)
		val defaultValue = values.firstOrNull()

		return values.mapIndexed { index, v ->
			val formattedValue = ValueFormatter.format(v, index, true, wrap)
			val cssClass = when {
				v == null -> "value-default"  // Using default placeholder
				index == 0 -> ""  // Default value itself, no special styling
				v != defaultValue -> "value-different"  // Different from default
				else -> ""  // Same as default, normal styling
			}

			val wrappedValue = if (cssClass.isNotEmpty()) {
				"<span class=\"$cssClass\">$formattedValue</span>"
			} else {
				formattedValue
			}

			"<span class=\"label\">${names[index]}:</span>" + wrappedValue
		}.joinToString("<br/>")
	}

	override fun fileExtension() = "html"
}
