plugins {
	id("java-gradle-plugin")
	id("org.jetbrains.kotlin.jvm") version "1.7.21"
	id("maven-publish")
	id("com.gradle.plugin-publish") version "1.0.0"
}

group = "io.github.thomo.valuestable.plugin"
version = "1.1.1"

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom:${findProperty("kotlinVersion")}"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${findProperty("kotlinVersion")}")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${findProperty("jacksonVersion")}")

	testImplementation("org.jetbrains.kotlin:kotlin-test:${findProperty("kotlinVersion")}")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${findProperty("kotlinVersion")}")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
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
