@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.9.0"
	id("maven-publish")
	id("com.gradle.plugin-publish") version "1.2.0"
}

group = "io.github.thomo.valuestable.plugin"
version = "1.4.1"

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom:${findProperty("kotlinVersion")}"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${findProperty("kotlinVersion")}")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${findProperty("kotlinVersion")}")
	testImplementation("org.hamcrest:hamcrest:2.2")
}

gradlePlugin {
	website.set("https://github.com/thomo/valuestable")
	vcsUrl.set("https://github.com/thomo/valuestable.git")
	val valuesTable by plugins.creating {
		id = "io.github.thomo.valuestable"
		displayName = "Plugin to create a table of defined helm values"
		description = "Creates an overview of helm values defined in multiple files"
		tags = listOf("yaml", "helm", "values", "markdown")
		implementationClass = "io.github.thomo.valuestable.plugin.ValuesTablePlugin"
	}
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest by tasks.registering(Test::class) {
	useJUnitPlatform()
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

tasks.test { useJUnitPlatform() }
