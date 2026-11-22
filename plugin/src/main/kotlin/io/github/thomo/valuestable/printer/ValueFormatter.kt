package io.github.thomo.valuestable.printer

object ValueFormatter {
    const val MASK = "***"

    fun format(value: String?, index: Int, isHtml: Boolean, wrap: Int = 0, masked: Boolean = false): String {
        if (value != null) {
            return if (!masked) wrap(value, isHtml, wrap) else if (isHtml) MASK else MASK.replace("*", "\\*")
        }

        val text = if (index == 0) "(n.d.)" else "default"
        return if (isHtml) "<i>$text</i>" else "*$text*"
    }

    // Adds a <wbr> break opportunity before each path separator. Dots escaped as `\.` are part of a
    // key segment (see ValueReader) and don't get one.
    fun formatKey(key: String, isHtml: Boolean) = splitKey(key).joinToString("<wbr>.") { escape(it, isHtml) }

    // Escapes text taken from values files or the build script (keys, values, env and chart names)
    // so it can't inject markup into the report.
    fun escape(text: String, isHtml: Boolean) = escapeUnits(text, isHtml).joinToString("")

    // Inserts <wbr> break opportunities every `wrap` characters so long unbroken tokens (base64
    // blobs, long URLs, ...) don't force wide table columns. `wrap <= 0` disables wrapping. Chunks
    // are counted in escaped code points, so a break never splits an entity or a surrogate pair.
    private fun wrap(text: String, isHtml: Boolean, wrap: Int): String {
        val units = escapeUnits(text, isHtml)
        if (wrap <= 0) return units.joinToString("")
        return units.chunked(wrap).joinToString("<wbr>") { it.joinToString("") }
    }

    private fun escapeUnits(text: String, isHtml: Boolean) =
        text.codePoints().toArray().map { escapeCodePoint(it, isHtml) }

    // Markdown output is mostly rendered as HTML, so it gets the HTML entities too, plus escapes
    // for what would break a table row (`|`, newlines) or be swallowed by the renderer (`\`).
    private fun escapeCodePoint(codePoint: Int, isHtml: Boolean) = when (codePoint) {
        '&'.code -> "&amp;"
        '<'.code -> "&lt;"
        '>'.code -> "&gt;"
        '|'.code -> if (isHtml) "|" else "\\|"
        '\\'.code -> if (isHtml) "\\" else "\\\\"
        '\n'.code -> if (isHtml) "\n" else "<br/>"
        '\r'.code -> if (isHtml) "\r" else ""
        else -> Character.toString(codePoint)
    }

    private fun splitKey(key: String): List<String> {
        val segments = mutableListOf(StringBuilder())
        var i = 0
        while (i < key.length) {
            val c = key[i]
            when {
                c == '\\' && i + 1 < key.length -> segments.last().append(c).append(key[++i])
                c == '.' -> segments.add(StringBuilder())
                else -> segments.last().append(c)
            }
            i++
        }
        return segments.map { it.toString() }
    }
}
