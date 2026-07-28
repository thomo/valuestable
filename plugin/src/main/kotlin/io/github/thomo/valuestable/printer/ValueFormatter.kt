package io.github.thomo.valuestable.printer

object ValueFormatter {
    fun format(value: String?, index: Int, isHtml: Boolean, wrap: Int = 0): String {
        if (value != null) {
            return wrap(value, wrap)
        }

        val text = if (index == 0) "(n.d.)" else "default"
        return if (isHtml) "<i>$text</i>" else "*$text*"
    }

    // Inserts <wbr> break opportunities every `wrap` characters so long unbroken tokens (base64
    // blobs, long URLs, ...) don't force wide table columns. `wrap <= 0` disables wrapping.
    private fun wrap(text: String, wrap: Int): String {
        if (wrap <= 0) return text
        return text.chunked(wrap).joinToString("<wbr>")
    }
}
