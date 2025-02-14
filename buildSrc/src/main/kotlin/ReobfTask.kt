import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

abstract class ReobfTask: Jar() {

  @get:Input
  open val spigotVersion: Property<String> = objectFactory.property(String::class.java)

  @get:InputFile
  open val inputFile: RegularFileProperty = objectFactory.fileProperty()

  @get:Input
  open val intermediaryClassifier: Property<String> = objectFactory.property(String::class.java).convention("mojang-mapped")

  @get:InputFile
  open val specialSource: Property<File> = objectFactory.property(File::class.java).convention(project.provider {
    project.configurations.named("spigotRemap").get().incoming.artifacts.artifacts
      .first { it.id.componentIdentifier.toString() == "net.md-5:SpecialSource:1.11.4" }.file
  })

  @get:Input
  open val mavenLocal: Property<File> = objectFactory.property(File::class.java).convention(project.provider {
    Paths.get(project.repositories.mavenLocal().url).toFile()
  })

  init {
    archiveClassifier.convention("reobf")
  }

  @TaskAction
  override fun copy() {
    val spigotVer = spigotVersion.get()
    val inFile = inputFile.get().asFile
    val obfPath = inFile.resolveSibling(inFile.name.replace(".jar", "-${intermediaryClassifier.get()}.jar"))

    // https://www.spigotmc.org/threads/510208/#post-4184317
    val specialSourceDir = specialSource.get().path
    val repo = mavenLocal.get()
    val spigotDir = repo.resolve("org/spigotmc/spigot/$spigotVer/")
    val mappingDir = repo.resolve("org/spigotmc/minecraft-server/$spigotVer/")

    // Remap original Mojang-mapped jar to obfuscated intermediary
    val mojangServer = spigotDir.resolve("spigot-$spigotVer-remapped-mojang.jar")
    val mojangMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-mojang.txt")
    remapPartial(specialSourceDir, mojangServer, mojangMappings, inFile, obfPath, true)

    // Remap obfuscated intermediary jar to Spigot and replace original
    val obfServer = spigotDir.resolve("spigot-$spigotVer-remapped-obf.jar")
    val spigotMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-spigot.csrg")
    remapPartial(specialSourceDir, obfServer, spigotMappings, obfPath, archiveFile.get().asFile, false)
  }

  private fun remapPartial(specialSource: String, serverJar: File, mapping: File, input: File, output: File, reverse: Boolean) {
    // TODO replace this with a direct dependency later?
    //   May not be a good idea without an isolated classloader.
    //   Would allow us to get rid of specialSource property.
    ProcessBuilder()
      .command(
        "java", "-cp", "$specialSource${File.pathSeparator}$serverJar",
        "net.md_5.specialsource.SpecialSource", "--live",
        "-i", input.path, "-o", output.path,
        "-m", "$mapping",
        if (reverse) "--reverse" else ""
      )
      .start().waitFor(60, TimeUnit.SECONDS)
  }

}
