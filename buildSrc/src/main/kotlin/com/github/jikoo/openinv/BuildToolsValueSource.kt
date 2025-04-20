package com.github.jikoo.openinv

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.File
import java.net.URI
import java.nio.file.Files
import javax.inject.Inject

abstract class BuildToolsValueSource: ValueSource<File, BuildToolsValueSource.Parameters> {

  @get:Inject
  abstract val exec: ExecOperations

  interface Parameters: ValueSourceParameters {
    val mavenLocal: Property<File>
    val workingDir: DirectoryProperty

    val spigotVersion: Property<String>
    val spigotRevision: Property<String>

    val ignoreCached: Property<Boolean>

    val javaHome: DirectoryProperty
    val javaExecutable: Property<String>
  }

  override fun obtain(): File {
    val version = parameters.spigotVersion.get()
    val revision = parameters.spigotRevision.get()
    val installLocation = getInstallLocation(version)
    // If Spigot is already installed, don't reinstall.
    if (!parameters.ignoreCached.get() && installLocation.exists()) {
      println("Skipping Spigot installation, $version is present")
      return installLocation
    }

    val buildTools = installBuildTools(parameters.workingDir.get().asFile)

    println("Installing Spigot $version (rev $revision)")

    exec.javaexec {
      environment["JAVA_HOME"] = parameters.javaHome.get()
      executable = parameters.javaExecutable.get()
      workingDir = buildTools.parentFile
      classpath(buildTools)
      args = listOf("--nogui", "--rev", revision, "--remapped")
    }.rethrowFailure()

    // Mark work for delete.
    cleanUp(buildTools.parentFile)

    if (!installLocation.exists()) {
      throw IllegalStateException(
        "Failed to install Spigot $version from $revision. Does the revision point to a different version?")
    }
    return installLocation
  }

  private fun getInstallLocation(version: String): File {
    return parameters.mavenLocal.get().resolve("org/spigotmc/spigot/$version/spigot-$version-remapped-mojang.jar")
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