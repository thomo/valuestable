package io.github.thomo.valuestable.printer

import kotlin.test.Test
import kotlin.test.assertEquals

class ValueFormatterTest {
	@Test
	fun `html escapes markup in values`() {
		assertEquals(
			"\"&lt;img src=x onerror=alert(1)&gt; &amp;\"",
			ValueFormatter.format("\"<img src=x onerror=alert(1)> &\"", 1, true)
		)
	}

	@Test
	fun `markdown escapes markup, pipes, backslashes and newlines`() {
		assertEquals("&lt;b&gt;a\\|b\\\\c<br/>d", ValueFormatter.escape("<b>a|b\\c\r\nd", false))
	}

	@Test
	fun `wrapping never splits an entity`() {
		assertEquals("&amp;&amp;<wbr>&amp;&amp;", ValueFormatter.format("&&&&", 1, true, 2))
	}

	@Test
	fun `wrapping never splits a surrogate pair`() {
		assertEquals("a😀<wbr>b", ValueFormatter.format("a😀b", 1, true, 2))
	}

	@Test
	fun `key breaks only at unescaped dots`() {
		assertEquals("ingress<wbr>.nginx\\.io/x", ValueFormatter.formatKey("ingress.nginx\\.io/x", true))
		assertEquals("ingress<wbr>.nginx\\\\.io/x", ValueFormatter.formatKey("ingress.nginx\\.io/x", false))
	}

	@Test
	fun `key markup is escaped`() {
		assertEquals("a<wbr>.&lt;/code&gt;&lt;script&gt;", ValueFormatter.formatKey("a.</code><script>", true))
	}

	@Test
	fun `masked values are hidden, undefined values are not`() {
		assertEquals("***", ValueFormatter.format("\"s3cr3t\"", 1, true, masked = true))
		assertEquals("\\*\\*\\*", ValueFormatter.format("\"s3cr3t\"", 1, false, masked = true))
		assertEquals("<i>default</i>", ValueFormatter.format(null, 1, true, masked = true))
	}
}
