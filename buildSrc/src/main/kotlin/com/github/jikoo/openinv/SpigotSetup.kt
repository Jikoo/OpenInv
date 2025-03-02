package com.github.jikoo.openinv

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.create
import java.nio.file.Paths
import javax.inject.Inject

abstract class SpigotSetup: Plugin<Project> {

  @get:Inject
  abstract val javaToolchainService: JavaToolchainService

  override fun apply(target: Project) {
    target.plugins.apply("java")

    val spigotExt = target.dependencies.extensions.create(
      "spigot",
      SpigotDependencyExtension::class.java,
      target.objects
    )

    val mvnLocal = target.repositories.mavenLocal()

    target.afterEvaluate {
      spigotExt.java.convention(target.extensions.getByType(JavaPluginExtension::class.java).toolchain)
      val launcher = javaToolchainService.launcherFor(spigotExt.java.get()).get()

      // Install Spigot with BuildTools.
      target.providers.of(BuildToolsValueSource::class.java) {
        parameters {
          mavenLocal.set(Paths.get(mvnLocal.url).toFile())
          workingDir.set(target.layout.buildDirectory.dir("tmp/buildtools"))
          spigotVersion.set(spigotExt.version)
          spigotRevision.set(spigotExt.revision)
          ignoreCached.set(spigotExt.ignoreCached)
          javaHome.set(launcher.metadata.installationPath)
          javaExecutable.set(launcher.executablePath.asFile.path)
        }
      }.get()

      // Add Spigot dependency.
      val dependency = target.dependencies.create(
        "org.spigotmc",
        "spigot",
        spigotExt.version.get(),
        spigotExt.configuration.orNull,
        spigotExt.classifier.orNull,
        spigotExt.ext.orNull)
      target.dependencies.add("compileOnly", dependency)
    }
  }

}
