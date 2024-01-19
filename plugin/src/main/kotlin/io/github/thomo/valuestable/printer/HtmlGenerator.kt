package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.model.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HtmlGenerator : Generator {
	override fun generate(collector: ValueCollector) = mutableListOf(
		"""
			<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
			<html>
			<head>
			<META http-equiv="Content-Type" content="text/html; charset=utf-8">
			<style>
				body {font-family: Arial, sans-serif;}
				h1 {color: #333;}
				table {border-collapse: collapse; width: 100%;}
				th, td {border: 1px solid #ddd; padding: 8px; text-align: left;}
				th {background-color: #f2f2f2;}
			</style>
			</head>
			""".trimIndent(),
		"<body><h1>Values</h1><p>generated at " + LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>",
		"<table>"
	) + generateTableHead() + "<tbody>" + generateTableRows(collector) +
		"</tbody></table></body></html>"

	fun generateTableHead() =
		"<thead><tr><th>key</th><th>values</th></tr></thead>"

	fun generateTableRows(collector: ValueCollector) =
		collector.keys().map { key -> "<tr>" + generateTableRow(key, collector) + "</tr>" }

	fun generateTableRow(key: String, collector: ValueCollector): String {
		val names = collector.getNames()
		return "<td>$key</td><td>" +
			collector.getValues(key)
				.mapIndexed { index, v -> names[index] + ": " + (v ?: if (index == 0) "<i>(n.d.)</i>" else "<i>default</i>") }
				.joinToString("<br/>", postfix = "</td>")
	}

	override fun fileExtension() = "html"
}
