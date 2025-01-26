plugins {
  `java-library`
  alias(libs.plugins.paperweight) apply false
  alias(libs.plugins.shadow) apply false
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

// Allow submodules to target higher Java release versions.
// Not currently necessary (as lowest supported version is in the 1.21 range)
// but may become relevant in the future.
java.disableAutoTargetJvm()

// Task to delete ./dist where final files are output.
tasks.register("cleanDist") {
  delete("dist")
}

tasks.clean {
  // Also delete distribution folder when cleaning.
  dependsOn(tasks.named("cleanDist"))
}
