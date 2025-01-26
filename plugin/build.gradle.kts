plugins {
  `openinv-base`
  alias(libs.plugins.shadow)
}

repositories {
  maven("https://jitpack.io")
}

dependencies {
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))
  implementation(project(":openinvadaptercommon"))
  implementation(project(":openinvadapterspigot", configuration = "shadow"))
  implementation(libs.planarwrappers)
}

tasks.processResources {
  expand("version" to version)
}

tasks.jar {
  manifest.attributes("paperweight-mappings-namespace" to "mojang")
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
  minimize {
    exclude(":openinv**")
  }
}

// TODO copy final file to a better location - dist folder for convenience with scripts?
