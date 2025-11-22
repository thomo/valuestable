package io.github.thomo.valuestable.printer

object ValueFormatter {
    fun format(value: String?, index: Int, isHtml: Boolean): String {
        if (value != null) {
            return value
        }
        
        val text = if (index == 0) "(n.d.)" else "default"
        return if (isHtml) "<i>$text</i>" else "*$text*"
    }
}
