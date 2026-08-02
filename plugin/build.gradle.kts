import org.gradle.plugin.compatibility.compatibility

plugins {
	id("java-gradle-plugin")
	alias(libs.plugins.kotlin.jvm)
	id("maven-publish")
	alias(libs.plugins.plugin.publish)
}

group = "io.github.thomo.valuestable.plugin"
version = "1.6.1"

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

	implementation(libs.jackson.yaml)
	implementation(libs.jackson.kotlin)

	testImplementation(libs.kotlin.test)
	testImplementation(libs.hamcrest)
	testImplementation(gradleTestKit())
}

gradlePlugin {
	website.set("https://github.com/thomo/valuestable")
	vcsUrl.set("https://github.com/thomo/valuestable.git")
	plugins.create("valuesTable") {
		id = "io.github.thomo.valuestable"
		displayName = "Plugin to create a condensed overview of Helm values"
		description = "Creates a Markdown/HTML overview comparing Helm values across environments and charts"
		tags = listOf("yaml", "helm", "values", "markdown")
		implementationClass = "io.github.thomo.valuestable.plugin.ValuesTablePlugin"
		compatibility {
			features {
				configurationCache = true
			}
		}
	}
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest = tasks.register<Test>("functionalTest") {
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
