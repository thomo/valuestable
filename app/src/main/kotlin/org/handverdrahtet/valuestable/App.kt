package org.handverdrahtet.valuestable

class App {
	val greeting: String
		get() {
			return "Hello World!"
		}
}

fun main() {
	println(App().greeting)
}
