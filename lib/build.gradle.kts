plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("java-library")
}

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
