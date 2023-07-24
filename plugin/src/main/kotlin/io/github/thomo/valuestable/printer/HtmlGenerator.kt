package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.ValueCollector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HtmlGenerator : Generator {
	override fun generate(collector: ValueCollector) = mutableListOf(
		"""
			<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
			<html>
			<head>
			<META http-equiv="Content-Type" content="text/html; charset=utf-8">
			<style></style>
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
				.mapIndexed { index, v -> names[index] + ": " + (v ?: if (index == 0) "*(n.d.)*" else "*default*") }
				.joinToString("<br/>", postfix = "</td>")
	}

	override fun fileExtension() = "html"
}
