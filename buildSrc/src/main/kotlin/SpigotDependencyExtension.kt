import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class SpigotDependencyExtension (
  private val dependencies: DependencyHandler,
  private val mavenLocal: Property<File>,
  private val tempDir: Provider<Directory>
) {

  fun add(
    version: String,
    revision: String = version.replace("-R\\d+\\.\\d+-SNAPSHOT".toRegex(), ""),
    configuration: String? = null,
    classifier: String? = "remapped-mojang",
    ext: String? = null,
    reinstall: Boolean = false,

  ): ExternalModuleDependency {
    installSpigot(revision, version, reinstall)
    val dependency = dependencies.create("org.spigotmc", "spigot", version, configuration, classifier, ext)
    dependencies.add("compileOnly", dependency)
    return dependency
  }

  private fun installSpigot(revision: String, version: String, reinstall: Boolean) {
    // If Spigot is already installed, don't reinstall.
    if (!reinstall && isInstalled(version)) {
      return
    }

    val buildTools = installBuildTools(tempDir.get().asFile)

    println("Installing Spigot $version (rev $revision)")
    val process = ProcessBuilder()
      .directory(buildTools.parentFile)
      .command("java", "-jar", buildTools.name, "--rev", revision, "--remapped")
      .inheritIO()
      .start()

    try {
      if (process.onExit().get(10L, TimeUnit.MINUTES).exitValue() != 0) {
        throw IllegalStateException("BuildTools installation did not complete successfully!")
      }
    } catch (e: TimeoutException) {
      // TODO see if manually creating Gradle's internal Java representation works
      //   Alternately can hack together something more gross and manual
      // Java's ProcessBuilder hangs if output is not consumed. We attempt to bypass this by
      // passing the output back to the caller, but apparently the daemon running makes this
      // an absolute mess.
      // The ProcessBuilder does appear to successfully run, because Spigot is built correctly,
      // but the process hangs post-completion.
      // https://github.com/gradle/gradle/issues/16716
      // https://leo3418.github.io/2021/06/20/java-processbuilder-stdout.html
      // Does not appear to be limited to Windows, but the Windows version is worse.
      process.destroyForcibly()
    }

    // Mark work for delete.
    cleanUp(buildTools.parentFile)

    if (!isInstalled(version)) {
      throw IllegalStateException(
        "Failed to install Spigot $version from $revision. Does the revision point to a different version?")
    }
  }

  private fun isInstalled(version: String): Boolean {
    return mavenLocal.get().resolve("org/spigotmc/spigot/$version/spigot-$version-remapped-mojang.jar").exists()
  }

  private fun installBuildTools(workingDir: File): File {
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

  private fun cleanUp(dir: File) {
    dir.deleteOnExit()
    if (!dir.isDirectory) {
      return
    }

    dir.listFiles()?.forEach {
      if (it.isDirectory) {
        cleanUp(it)
      } else {
        it.deleteOnExit()
      }
    }
  }

}
