import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp") version "1.6.20-1.0.4" // used for plugin-processor
    kotlin("jvm") version "1.6.10"
    // used for json-serialization and deserialization
    kotlin("plugin.serialization") version "1.6.10"
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.7.0"
}

val experimentalAnnotations =
    listOf("kotlin.RequiresOptIn", "kotlin.time.ExperimentalTime", "kotlin.contracts.ExperimentalContracts")

version = "1.0.11"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    maven("https://nycode.jfrog.io/artifactory/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // this one is included in the bot itself, therefore we make it compileOnly
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("dev.schlaubi", "mikbot-api", "2.5.0-SNAPSHOT")


    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.0.0")

    // implementation("io.ktor", "ktor-serialization", "1.6.2")
}

mikbotPlugin {
    description.set("Kartell Bot")
    provider.set("niggelgame")
    license.set("AGPL-3.0")
}

tasks {
    task<Copy>("buildAndCopy") {
        dependsOn(assemblePlugin)
        from(assemblePlugin)
        include("*.zip")
        into("plugins")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + experimentalAnnotations.map { "-Xopt-in=$it" }
        }
    }

    installBot {
        botVersion.set("2.5.0-SNAPSHOT")
    }
}

kotlin {
    jvmToolchain {
        (this as DefaultToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

pluginPublishing {
    // The address your repository is hosted it
    // if you use Git LFS and GitHub Pages use https://github.com/owner/repo/raw/branch
    repositoryUrl.set("https://github.com/niggelgame/kreidebot/raw/plugin-repo")
    // The directory the generated repository should be in
    targetDirectory.set(rootProject.file("ci-repo").toPath())
    // The URL of the project
    projectUrl.set("https://github.com/niggelgame/kreidebot")
}