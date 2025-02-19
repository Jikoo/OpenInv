import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

abstract class InstallSpigotTask @Inject constructor(
  private val execActionFactory: ExecActionFactory,
  objectFactory: ObjectFactory
) : DefaultTask() {

  @get:Input
  val reinstall = objectFactory.property(Boolean::class.java).convention(false)

  @get:Input
  val spigotRevision = objectFactory.property(String::class.java)

  @get:Input
  val spigotVersion = objectFactory.property(String::class.java)

  private val mavenLocal: Property<File> = objectFactory.property(File::class.java).convention(project.provider {
    Paths.get(project.repositories.mavenLocal().url).toFile()
  })

  @TaskAction
  fun installSpigot() {
    if (reinstall.get() || isInstalled()) {
      return
    }

    val buildTools = installBuildTools()
    val exec = execActionFactory.newJavaExecAction()
    exec.classpath(buildTools)
    exec.args("--rev", spigotRevision.get(), "--remapped")
    exec.execute().rethrowFailure()

    if (!isInstalled()) {
      throw IllegalStateException(
        "Failed to install Spigot $spigotVersion from $spigotRevision. Does the revision point to a different version?")
    }
  }

  private fun isInstalled() : Boolean {
    val spigotVer = spigotVersion.get()
    return mavenLocal.get().resolve("org/spigotmc/spigot/$spigotVer/spigot-$spigotVer-remapped-mojang.jar").exists()
  }

  private fun installBuildTools(): File {
    val workingDir = temporaryDirFactory.create()?.resolve("buildtools") ?: throw IllegalStateException()

    val buildTools = workingDir.resolve("BuildTools.jar")
    if (buildTools.exists()) {
      return buildTools
    }

    workingDir.mkdirs()

    val buildToolsUrl = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
    println("Downloading $buildToolsUrl")
    val stream = URI.create(buildToolsUrl).toURL().openStream()
    Files.copy(stream, buildTools.toPath())
    stream.close()

    return buildTools
  }

}
