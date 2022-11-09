plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	id("maven-publish")
	id("com.gradle.plugin-publish") version "1.0.0"
}

group = "io.github.thomo.valuestable.plugin"
version = "1.1.1"

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.0-rc3")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0-rc3")

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
		id = "io.github.thomo.valuestable"
		displayName = "Plugin to create a table of defined helm values"
		description = "Creates an overview of helm values defined in multiple files"
		implementationClass = "io.github.thomo.valuestable.plugin.ValuesTablePlugin"
	}
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest by tasks.registering(Test::class) {
	testClassesDirs = functionalTestSourceSet.output.classesDirs
	classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
	dependsOn(functionalTest)
}

publishing {
	repositories {
		mavenLocal()
	}
}
