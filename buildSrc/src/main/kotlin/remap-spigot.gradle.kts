import java.nio.file.Path
import java.nio.file.Paths

val spigotRemap = configurations.create("spigotRemap")

repositories {
  mavenCentral()
}

dependencies {
  spigotRemap("net.md-5:SpecialSource:1.11.4:shaded")
}

abstract class RemapTask
  @Inject constructor(private val project: Project): DefaultTask() {

  @get:Input
  abstract var spigotVersion: String

  @TaskAction
  fun remapInputs() {
    inputs.files.forEach {
      remap(spigotVersion, it.toPath(), it.toPath().resolveSibling(it.name.replace(".jar", "-obf.jar")))
    }
  }

  private fun remap(spigotVersion: String, jarPath: Path, obfPath: Path) {
    // https://www.spigotmc.org/threads/510208/#post-4184317
    val specialsource = project.configurations.named("spigotRemap").get().incoming.artifacts.artifacts
      .first { it.id.componentIdentifier.toString() == "net.md-5:SpecialSource:1.11.4" }.file.path
    val repo = Paths.get(project.repositories.mavenLocal().url)
    val spigotDir = repo.resolve("org/spigotmc/spigot/$spigotVersion/")
    val mappingDir = repo.resolve("org/spigotmc/minecraft-server/$spigotVersion/")

    // Remap original Mojang-mapped jar to obfuscated intermediary
    val mojangServer = spigotDir.resolve("spigot-$spigotVersion-remapped-mojang.jar")
    val mojangMappings = mappingDir.resolve("minecraft-server-$spigotVersion-maps-mojang.txt")
    remapPartial(specialsource, mojangServer, mojangMappings, jarPath, obfPath, true)

    // Remap obfuscated intermediary jar to Spigot and replace original
    val obfServer = spigotDir.resolve("spigot-$spigotVersion-remapped-obf.jar")
    val spigotMappings = mappingDir.resolve("minecraft-server-$spigotVersion-maps-spigot.csrg")
    remapPartial(specialsource, obfServer, spigotMappings, obfPath, jarPath, false)
  }

  private fun remapPartial(specialSource: String, serverJar: Path, mapping: Path, input: Path, output: Path, reverse: Boolean) {
    project.providers.exec {
      commandLine("java", "-cp", "$specialSource${File.pathSeparator}$serverJar",
        "net.md_5.specialsource.SpecialSource", "--live",
        "-i", "$input", "-o", "$output",
        "-m", "$mapping",
        if (reverse) "--reverse" else "")
    }.result.get()
  }
}
