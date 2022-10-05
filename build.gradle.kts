import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.0.0"
    kotlin("jvm") version "1.7.10"
}

group = "dev.sparky200"
version = "1.1.2"

pluginBundle {
    website = "https://necessegame.com/"
    vcsUrl = "https://github.com/Sparky200/NecesseBuildDsl"
    tags = listOf("necesse", "build", "dsl", "mod", "modding")
}

gradlePlugin {
    plugins {
        create("necesseBuildDsl") {
            id = "dev.sparky200.necesse-build-dsl"
            displayName = "Necesse Mods Build DSL"
            description = "DSL and provided tasks for Necesse Mods"
            implementationClass = "dev.sparky200.necesse.gradle.build_dsl.NecesseBuildDsl"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}