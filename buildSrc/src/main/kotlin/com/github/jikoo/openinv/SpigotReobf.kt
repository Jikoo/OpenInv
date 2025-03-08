package com.github.jikoo.openinv

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.nio.file.Paths

class SpigotReobf: Plugin<Project> {

  companion object {
    const val ARTIFACT_CONFIG = "reobf"
  }

  override fun apply(target: Project) {
    // Re-use extension from Spigot dependency declaration if available to reduce configuration requirements.
    val spigotExt = target.dependencies.extensions.findByType(SpigotDependencyExtension::class.java)
      ?: target.dependencies.extensions.create(
        "spigot",
        SpigotDependencyExtension::class.java,
        target.objects
      )

    val mvnLocal = target.repositories.mavenLocal()

    val reobfTask = target.tasks.register<SpigotReobfTask>("reobfTask") {
      dependsOn(target.tasks.named("shadowJar"))
      // ShadowJar extends Jar, so this should be a safe way to get the result without having
      // to jump through hoops and shift around shadow declarations in the rest of the project.
      inputFile.convention(target.tasks.named<Jar>("shadowJar").get().archiveFile)
      spigotVersion.convention(spigotExt.version)
      getMavenLocal().set(Paths.get(mvnLocal.url).toFile())
    }

    // Set up configuration for producing reobf jar.
    target.configurations.consumable(ARTIFACT_CONFIG) {
      attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, target.objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, target.objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, target.objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, target.objects.named(LibraryElements.JAR))
      }
    }

    // Add artifact from reobf task.
    target.artifacts {
      add(ARTIFACT_CONFIG, reobfTask)
    }
  }

}
