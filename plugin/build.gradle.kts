@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("java-gradle-plugin")
	alias(libs.plugins.kotlin.jvm)
	id("maven-publish")
	alias(libs.plugins.plugin.publish)
}

group = "io.github.thomo.valuestable.plugin"
version = "1.5.2"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<JavaCompile> {
	sourceCompatibility = "21"
	targetCompatibility = "21"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform(libs.kotlin.bom))
	implementation(libs.kotlin.stdlib)

	implementation(libs.jackson.yaml)
	implementation(libs.jackson.kotlin)

	testImplementation(libs.kotlin.test)
	testImplementation(libs.hamcrest)
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
