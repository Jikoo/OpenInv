package com.github.jikoo.openinv

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.create
import org.gradle.process.ExecOperations
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import java.io.File
import java.net.URI
import java.nio.file.Files
import javax.inject.Inject

abstract class SpigotBuildService: BuildService<SpigotBuildService.Parameters>, AutoCloseable, OperationCompletionListener {

  @get:Inject
  abstract val exec: ExecOperations

  interface Parameters: BuildServiceParameters {
    val mavenLocal: Property<File>
    val workingDir: DirectoryProperty
  }

  override fun onFinish(event: FinishEvent?) {
    // Keep service alive until needed.
  }

  override fun close() {
    // Also needed for service to stay alive, probably
  }

  fun addDependencies(spigotExt: SpigotDependencyExtension) {
    if (!spigotExt.version.isPresent) {
      return
    }
    val dependencies = spigotExt.dependencies.get()
    val dependency = dependencies.create(
      "org.spigotmc",
      "spigot",
      spigotExt.version.get(),
      spigotExt.configuration.orNull,
      spigotExt.classifier.orNull,
      spigotExt.ext.orNull)
    dependencies.add("compileOnly", dependency)
  }

  fun generateArtifacts(
    spigotExt: SpigotDependencyExtension,
    toolchainService: JavaToolchainService) {
    if (!spigotExt.version.isPresent) {
      // TODO should this be removed for a more aggressive failure?
      println("Skipping Spigot installation, version is unset")
      return
    }
    val version = spigotExt.version.get()
    val revision = spigotExt.revision.get()
    // If Spigot is already installed, don't reinstall.
    if (!spigotExt.ignoreCached.get() && isInstalled(version)) {
      println("Skipping Spigot installation, $version is present")
      return
    }

    val buildTools = installBuildTools(parameters.workingDir.get().asFile)

    println("Installing Spigot $version (rev $revision)")

    val launcher = toolchainService.launcherFor(spigotExt.java.get()).get()
    exec.javaexec {
      environment["JAVA_HOME"] = launcher.metadata.installationPath
      executable = launcher.executablePath.asFile.path
      workingDir = buildTools.parentFile
      classpath(buildTools)
      args = listOf("--nogui", "--rev", revision, "--remapped")
    }.rethrowFailure()

    // Mark work for delete.
    cleanUp(buildTools.parentFile)

    if (!isInstalled(version)) {
      throw IllegalStateException(
        "Failed to install Spigot $version from $revision. Does the revision point to a different version?")
    }
  }

  private fun isInstalled(version: String): Boolean {
    return parameters.mavenLocal.get().resolve("org/spigotmc/spigot/$version/spigot-$version-remapped-mojang.jar").exists()
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
