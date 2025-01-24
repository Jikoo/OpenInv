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

tasks.assemble {
  //dependsOn(tasks.shadowJar)
}

tasks.jar {
  manifest.attributes("paperweight-mappings-namespace" to "mojang")
}
