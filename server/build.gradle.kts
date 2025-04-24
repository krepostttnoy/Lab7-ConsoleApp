plugins {
    kotlin("jvm")
    id("application")
    kotlin("plugin.serialization") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.example.ServerKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

kotlin {
    jvmToolchain(23)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}