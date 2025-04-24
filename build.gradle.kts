plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

group = "org.example"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        // Общие зависимости для всех модулей
        implementation(kotlin("stdlib"))
        implementation("com.google.code.gson:gson:2.10.1") // Пример для JSON
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // JUnit5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register("runServer") {
    dependsOn(":server:run")
    doLast {
        println("Server is running...")
    }
}

tasks.register("runClient") {
    dependsOn(":client:run")
}

tasks.register("runAll"){
    dependsOn(":server:run")
    dependsOn(":client:run")
}