package com.github.jikoo.openinv

import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.RemapperProcessor
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class SpigotReobfTask: org.gradle.api.tasks.bundling.Jar() {

  @get:Input
  val spigotVersion: Property<String> = objectFactory.property(String::class.java)

  @get:InputFile
  val inputFile: RegularFileProperty = objectFactory.fileProperty()

  @get:Input
  val intermediaryClassifier: Property<String> = objectFactory.property(String::class.java).convention("mojang-mapped")

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
    val repo = mavenLocal.get()
    val spigotDir = repo.resolve("org/spigotmc/spigot/$spigotVer/")
    val mappingDir = repo.resolve("org/spigotmc/minecraft-server/$spigotVer/")

    // Remap original Mojang-mapped jar to obfuscated intermediary
    val mojangServer = spigotDir.resolve("spigot-$spigotVer-remapped-mojang.jar")
    val mojangMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-mojang.txt")
    remapPartial(mojangServer, mojangMappings, inFile, obfPath, true)

    // Remap obfuscated intermediary jar to Spigot and replace original
    val obfServer = spigotDir.resolve("spigot-$spigotVer-remapped-obf.jar")
    val spigotMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-spigot.csrg")
    remapPartial(obfServer, spigotMappings, obfPath, archiveFile.get().asFile, false)
  }

  private fun remapPartial(server: File, mapping: File, input: File, output: File, reverse: Boolean) {
    val jarMapping = JarMapping()
    jarMapping.loadMappings(mapping.path, reverse, false, null, null)

    val inheritance = JointProvider()
    jarMapping.setFallbackInheritanceProvider(inheritance)

    // Equivalent of --live with server jar on classpath.
    val serverJar = Jar.init(server)
    inheritance.add(JarProvider(serverJar))

    val inputJar = Jar.init(input)
    inheritance.add(JarProvider(inputJar))

    // Remap reflective access.
    val preprocessor = RemapperProcessor(null, jarMapping, null)

    val remapper = JarRemapper(preprocessor, jarMapping, null)
    remapper.remapJar(inputJar, output, emptySet())

    serverJar.close()
    inputJar.close()
  }

  @Internal
  internal fun getMavenLocal(): Property<File> {
    return mavenLocal
  }

}
