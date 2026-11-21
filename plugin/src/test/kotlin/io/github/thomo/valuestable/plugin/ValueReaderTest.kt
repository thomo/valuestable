package io.github.thomo.valuestable.plugin

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueReaderTest {
	@field:TempDir
	lateinit var tempFolder: File

	private fun read(yaml: String) = File(tempFolder, "values.yaml")
		.apply { writeText(yaml.trimIndent()) }
		.let { ValueReader().read(it.toPath()) }

	@Test
	fun `flattens nested keys`() {
		val values = read(
			"""
			root:
			  a: aaa
			  sub:
			    b: 1
			    n: null
			"""
		)

		assertEquals(mapOf("root.a" to "\"aaa\"", "root.sub.b" to "1", "root.sub.n" to null), values)
	}

	@Test
	fun `literal dotted key does not collide with nested path`() {
		val values = read(
			"""
			nginx.ingress.enabled: false
			nginx:
			  ingress:
			    enabled: true
			"""
		)

		assertEquals(mapOf("nginx\\.ingress\\.enabled" to "false", "nginx.ingress.enabled" to "true"), values)
	}

	@Test
	fun `backslash in key is escaped`() {
		assertEquals(mapOf("a\\\\.b" to "1"), read("""a\: { b: 1 }"""))
	}

	@Test
	fun `reads many keys`() {
		val values = read((1..50_000).joinToString("\n") { "k$it: $it" })

		assertEquals(50_000, values.size)
	}
}
