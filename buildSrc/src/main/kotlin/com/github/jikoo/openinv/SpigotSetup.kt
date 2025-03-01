package com.github.jikoo.openinv

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainService
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

    val builder = target.gradle.sharedServices.registerIfAbsent("spigotSetup", SpigotBuildService::class.java) {
      parameters {
        // TODO this "secretly" adds the maven local repo. Make it clearer?
        mavenLocal.convention(Paths.get(target.repositories.mavenLocal().url).toFile())
        workingDir.convention(target.layout.buildDirectory.dir("tmp/buildtools"))
      }
    }

    //target.tasks.register("spigotSetup", SpigotSetupTask::class.java)

    target.afterEvaluate {
      spigotExt.java.convention(target.extensions.getByType(JavaPluginExtension::class.java).toolchain)
      spigotExt.dependencies.convention(target.dependencies)

      val setup = builder.get()
      setup.generateArtifacts(spigotExt, javaToolchainService)
      setup.addDependencies(spigotExt)
    }
  }

}
