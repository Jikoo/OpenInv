import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import java.nio.file.Paths
import javax.inject.Inject

abstract class ReobfTask
  @Inject constructor(private var execActionFactory: ExecActionFactory): Jar() {

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
    exec.classpath = project.files(specialSourceFile, serverJar)
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

}
