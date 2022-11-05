plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	id("maven-publish")
	id("com.gradle.plugin-publish") version "1.0.0"
}

group = "org.handverdrahtet.plugin.valuestable"
version = "1.0.0"

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation(project(":lib"))

	testImplementation("org.jetbrains.kotlin:kotlin-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

pluginBundle {
	website = "https://github.com/thomo/valuestable"
	vcsUrl = "https://github.com/thomo/valuestable.git"
	tags = listOf("yaml", "helm", "values", "markdown")
}

gradlePlugin {
	val valuesTable by plugins.creating {
		id = "org.handverdrahtet.plugin.valuestable"
		displayName = "Plugin to create a table of defined helm values"
		description = "Creates an overview of helm values defined in multiple files"
		implementationClass = "org.handverdrahtet.plugin.ValuesTablePlugin"
	}
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
	testClassesDirs = functionalTestSourceSet.output.classesDirs
	classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
	// Run the functional tests as part of `check`
	dependsOn(functionalTest)
}
