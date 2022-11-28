package io.github.thomo.valuestable.printer

import io.github.thomo.valuestable.ValueCollector

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
		"<body><h1>Values</h1><p>generated at 2022-11-15 13:35:34</p>",
		"<table>"
	) + generateTableHead(collector) + "<tbody>" + generateTableRows(collector) +
		"</tbody></table></body></html>"

	fun generateTableHead(vc: ValueCollector) =
		mutableListOf("<thead><tr><th>key</th>") +
			vc.getNames()
				.map { name -> "<th style='text-align:center'>$name</th>" } +
			"</tr></thead>"

	fun generateTableRows(vc: ValueCollector) =
		vc.keys().map { key -> "<tr>" + generateTableRow(key, vc.getValues(key)) + "</tr>" }

	fun generateTableRow(key: String, values: List<String?>) =
		"<td>$key</td>" +
			values.map { v -> "<td style=\"text-align:center\">" + (v ?: "<span class=\"default\">default</span>") + "</td>" }
				.joinToString("")

	override fun fileExtension() = "html"
}
