rootProject.name = "valuestable"
include("lib", "app", "plugin")

pluginManagement {
	repositories {
		maven(url = "./my-plugins")
		gradlePluginPortal()
	}
}
