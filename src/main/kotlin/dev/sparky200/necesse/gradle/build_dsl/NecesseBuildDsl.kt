@file:Suppress("DEPRECATION", "unused")

package dev.sparky200.necesse.gradle.build_dsl

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer

@Suppress("LeakingThis")
abstract class NecesseBuildDslExtension {
    abstract val gamePath: Property<String>
    abstract val destinationDirectory: Property<String>
    abstract val targetSourceSet: Property<String>
    abstract val id: Property<String>
    abstract val name: Property<String>
    abstract val version: Property<String>
    abstract val gameVersion: Property<String>
    abstract val description: Property<String>
    abstract val author: Property<String>
    abstract val dependencies: Property<Array<String>>
    abstract val optionalDependencies: Property<Array<String>>
    abstract val clientSide: Property<Boolean>

    init {
        gamePath.convention("")
        destinationDirectory.convention("build/libs")
        targetSourceSet.convention("main")
        id.convention("")
        name.convention("")
        version.convention("")
        gameVersion.convention("")
        description.convention("")
        author.convention("")
        dependencies.convention(arrayOf())
        optionalDependencies.convention(arrayOf())
        clientSide.convention(false)
    }
}

@Suppress("unused")
class NecesseBuildDsl : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("necesse-build-dsl", NecesseBuildDslExtension::class.java)

        project.tasks.create("createAppID", DefaultTask::class.java) { it.apply {
            group = "necesse"
            description = "Creates steam_appid.txt file"

            doLast {
                project.file("steam_appid.txt").writeText("1169040")
            }
        } }

        project.tasks.create("createModInfo", JavaExec::class.java) {
            it.apply {
                description = "Creates the mod.info file."
                group = "necesse"

                main = "CreateModInfoFile"

                // has to be set just before the task
                doFirst {
                    classpath = project.files(extension.gamePath.get() + "/Necesse.jar")
                    args = mapOf(
                        "-file" to "build/info_out/mod.info",
                        "-id" to extension.id.get(),
                        "-name" to extension.name.get(),
                        "-version" to extension.version.get(),
                        "-gameVersion" to extension.gameVersion.get(),
                        "-description" to extension.description.get(),
                        "-author" to extension.author.get(),
                        "-clientside" to extension.clientSide.get().toString(),
                        "-dependencies" to extension.dependencies.get().joinToString(", ", "[", "]"),
                        "-optionalDependencies" to extension.optionalDependencies.get().joinToString(", ", "[", "]")
                    ).flatMap { pair -> listOf(pair.key, pair.value) }
                }

                doLast {
                    // add the mod info file to the resources source
                    project.sourceSets.asMap[extension.targetSourceSet.get()]!!.resources.srcDirs("build/info_out")
                }
            }
        }

        project.tasks.create("runClient", JavaExec::class.java) {
            it.apply {
                group = "necesse"
                description = "Runs the client with the current gradle project."
                dependsOn("createAppID", "build")


                main = "StartClient"
                jvmArgs = listOf(
                    "-Xms512m",
                    "-Xmx3G",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1ReservePercent=20",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:G1HeapRegionSize=32M"
                )

                doFirst {
                    classpath = project.files(extension.gamePath.get() + "/Necesse.jar")
                    args = listOf("-mod \"${extension.destinationDirectory.get()}\"")
                }
            }
        }

        project.tasks.create("runDevClient", JavaExec::class.java) {
            it.apply {
                group = "necesse"
                description = "Runs the client with the current gradle project in developer mode."
                dependsOn("createAppID", "build")

                main = "StartClient"
                jvmArgs = listOf(
                    "-Xms512m",
                    "-Xmx3G",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1ReservePercent=20",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:G1HeapRegionSize=32M"
                )

                doFirst {
                    classpath = project.files(extension.gamePath.get() + "/Necesse.jar")
                    args = listOf("-dev 1", "-mod \"${extension.destinationDirectory.get()}\"")
                }
            }
        }

        project.tasks.create("runServer", JavaExec::class.java) {
            it.apply {
                group = "necesse"
                description = "Runs the server with the current gradle project."
                dependsOn("build")

                main = "StartServer"
                jvmArgs = listOf(
                    "-Xms512m",
                    "-Xmx3G",
                    "-XX:+UnlockExperimentalVMOptions",
                    "-XX:+UseG1GC",
                    "-XX:G1NewSizePercent=20",
                    "-XX:G1ReservePercent=20",
                    "-XX:MaxGCPauseMillis=50",
                    "-XX:G1HeapRegionSize=32M"
                )

                doFirst {
                    classpath = project.files(extension.gamePath.get() + "/Server.jar")
                    args = listOf("-mod \"${extension.destinationDirectory.get()}\"")
                }
            }
        }

        try {
            project.tasks.getByName("classes").apply {
                dependsOn("createModInfo")
            }
        } catch (ignored: UnknownTaskException) {}
    }
}

private val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

inline fun Project.mod(block: NecesseBuildDslExtension.() -> Unit) = (object : TypeOf<NecesseBuildDslExtension>() {})
    .let { type ->
        convention.findByType(type)
            ?: convention.findPlugin(NecesseBuildDslExtension::class.java)
            ?: convention.getByType(type)
    }.block()

fun DependencyHandler.necesse(project: Project, gamePath: String) {
    add("compileOnly", project.files("$gamePath/Necesse.jar"))
    add("compileOnly", project.fileTree("$gamePath/lib/"))
    add("compileOnly", project.fileTree("./mods/"))
}