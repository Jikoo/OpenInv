import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

abstract class ReobfTask: Jar() {

  @get:Input
  open val spigotVersion: Property<String> = objectFactory.property(String::class.java)

  @get:InputFile
  open val inputFile: Property<RegularFile> = objectFactory.fileProperty()

  @get:Input
  open val intermediaryClassifier: Property<String> = objectFactory.property(String::class.java).convention("mojang-mapped")

  init {
    archiveClassifier.convention("reobf")
  }

  @TaskAction
  override fun copy() {
    val spigotVer = spigotVersion.get()
    val inFile = inputFile.get().asFile
    val obfPath = inFile.resolveSibling(inFile.name.replace(".jar", "-${intermediaryClassifier.get()}.jar"))

    // https://www.spigotmc.org/threads/510208/#post-4184317
    val specialsource = project.configurations.named("spigotRemap").get().incoming.artifacts.artifacts
      .first { it.id.componentIdentifier.toString() == "net.md-5:SpecialSource:1.11.4" }.file.path
    val repo = Paths.get(project.repositories.mavenLocal().url)
    val spigotDir = repo.resolve("org/spigotmc/spigot/$spigotVer/")
    val mappingDir = repo.resolve("org/spigotmc/minecraft-server/$spigotVer/")

    // Remap original Mojang-mapped jar to obfuscated intermediary
    val mojangServer = spigotDir.resolve("spigot-$spigotVer-remapped-mojang.jar")
    val mojangMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-mojang.txt")
    remapPartial(specialsource, mojangServer, mojangMappings, inFile, obfPath, true)

    // Remap obfuscated intermediary jar to Spigot and replace original
    val obfServer = spigotDir.resolve("spigot-$spigotVer-remapped-obf.jar")
    val spigotMappings = mappingDir.resolve("minecraft-server-$spigotVer-maps-spigot.csrg")
    remapPartial(specialsource, obfServer, spigotMappings, obfPath, archiveFile.get().asFile, false)
  }

  private fun remapPartial(specialSource: String, serverJar: Path, mapping: Path, input: File, output: File, reverse: Boolean) {
    project.providers.exec {
      commandLine("java", "-cp", "$specialSource${File.pathSeparator}$serverJar",
        "net.md_5.specialsource.SpecialSource", "--live",
        "-i", input.path, "-o", output.path,
        "-m", "$mapping",
        if (reverse) "--reverse" else "")
    }.result.get()
  }

}