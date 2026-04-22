plugins {
  `java-library`
  alias(libs.plugins.paperweight) apply false
  alias(libs.plugins.shadow) apply false
  alias(libs.plugins.errorprone.gradle)
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

// Allow submodules to target higher Java release versions.
java.disableAutoTargetJvm()

subprojects {
  apply(plugin = "java-library")
  apply(plugin = rootProject.libs.plugins.errorprone.gradle.get().pluginId)

  repositories {
    mavenCentral()
  }

  dependencies {
    compileOnly(rootProject.libs.jspecify)
    compileOnly(rootProject.libs.annotations)
    errorprone(rootProject.libs.errorprone.core)
  }

  tasks {
    withType<JavaCompile>().configureEach {
      options.encoding = Charsets.UTF_8.name()
    }
    withType<Javadoc>().configureEach {
      options.encoding = Charsets.UTF_8.name()
    }
  }
}

// Task to delete ./dist where final files are output.
tasks.register("cleanDist") {
  delete("dist")
}

tasks.clean {
  // Also delete distribution folder when cleaning.
  dependsOn(tasks.named("cleanDist"))
}
