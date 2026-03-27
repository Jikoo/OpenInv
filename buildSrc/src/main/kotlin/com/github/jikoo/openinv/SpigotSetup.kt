package com.github.jikoo.openinv

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainService
import java.io.File
import javax.inject.Inject

abstract class SpigotSetup : Plugin<Project> {

  @get:Inject
  abstract val javaToolchainService: JavaToolchainService

  override fun apply(target: Project) {
    target.plugins.apply("java")

    // Set up extension for configuring Spigot dependency.
    val spigotExt = target.dependencies.extensions.findByType(SpigotDependencyExtension::class.java)
      ?: target.dependencies.extensions.create(
        "spigot",
        SpigotDependencyExtension::class.java,
        target.objects
      )

    target.afterEvaluate {
      // Get Java requirements, defaulting to version used for compilation.
      spigotExt.java.convention(target.extensions.getByType(JavaPluginExtension::class.java).toolchain)
      val launcher = javaToolchainService.launcherFor(spigotExt.java.get()).get()

      // Install Spigot with BuildTools.
      val spigot: File = target.providers.of(BuildToolsValueSource::class.java) {
        parameters {
          installDir.set(target.gradle.gradleUserHomeDir.resolve("caches/spigot"))
          workingDir.set(target.layout.buildDirectory.dir("tmp/buildtools"))
          spigotVersion.set(spigotExt.version)
          spigotRevision.set(spigotExt.revision)
          ignoreCached.set(spigotExt.ignoreCached)
          javaHome.set(launcher.metadata.installationPath)
          javaExecutable.set(launcher.executablePath.asFile.path)
        }
      }.get()

      // Add Spigot dependency.
      val dependency = target.dependencies.create(files(spigot))
      target.dependencies.add("compileOnly", dependency)
    }
  }

}
