import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev764"
}

group = "isel.tds"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

val daggerVersion by extra("2.39.1")

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("org.mongodb:mongodb-driver-kotlin-sync:4.11.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "GoCompose"
            packageVersion = "1.0.0"
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.9"
}