package com.github.jikoo.openinv

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import javax.inject.Inject

abstract class SpigotReobfTask @Inject constructor(
  private var execActionFactory: ExecActionFactory
) : Jar() {

  @get:Input
  val spigotVersion: Property<String> = objectFactory.property(String::class.java)

  @get:InputFile
  val inputFile: RegularFileProperty = objectFactory.fileProperty()

  @get:Input
  val intermediaryClassifier: Property<String> = objectFactory.property(String::class.java).convention("mojang-mapped")

  private val specialSource: Property<File> = objectFactory.property(File::class.java).convention(project.provider {
    // Grab SpecialSource location from dependency declaration.
    project.configurations.named(SpigotReobf.DEP_CONFIG).get().incoming.artifacts.artifacts
      .first { it.id.componentIdentifier.toString().startsWith("net.md-5:SpecialSource:") }.file
  })

  private val mavenLocal: Property<File> = objectFactory.property(File::class.java)

  init {
    archiveClassifier.convention(SpigotReobf.ARTIFACT_CONFIG)
  }

  @TaskAction
  override fun copy() {
    val spigotVer = spigotVersion.get()
    val inFile = inputFile.get().asFile
    val obfPath = inFile.resolveSibling(inFile.name.replace(".jar", "-${intermediaryClassifier.get()}.jar"))

    // https://www.spigotmc.org/threads/510208/#post-4184317
    val specialSourceFile = specialSource.get()
    val repo = mavenLocal.get()
    val spigotDir = repo.resolve("org/spigotmc/spigot/$spigotVer/")
    val mappingDir = repo.resolve("org/spigotmc/minecraft-server/$spigotVer/")

    // Remap original Mojang-mapped jar to obfuscated intermediary
    val mojangServer = spigotDir.resolve("spigot-$spigotVer-remapped-mojang.jar")
    val mojangMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-mojang.txt")
    remapPartial(specialSourceFile, mojangServer, mojangMappings, inFile, obfPath, true)

    // Remap obfuscated intermediary jar to Spigot and replace original
    val obfServer = spigotDir.resolve("spigot-$spigotVer-remapped-obf.jar")
    val spigotMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-spigot.csrg")
    remapPartial(specialSourceFile, obfServer, spigotMappings, obfPath, archiveFile.get().asFile, false)
  }

  private fun remapPartial(specialSourceFile: File, serverJar: File, mapping: File, input: File, output: File, reverse: Boolean) {
    // May need a direct dependency on SpecialSource later to customize behavior.
    val exec = execActionFactory.newJavaExecAction()
    exec.classpath(specialSourceFile, serverJar)
    exec.mainClass.value("net.md_5.specialsource.SpecialSource")
    exec.args(
      "--live",
      "-i", input.path,
      "-o", output.path,
      "-m", "$mapping",
      if (reverse) "--reverse" else ""
    )
    exec.execute().rethrowFailure()
  }

  @Internal
  internal fun getMavenLocal(): Property<File> {
    return mavenLocal
  }

}
