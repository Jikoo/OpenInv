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
  implementation(project(":openinvadapterpaper1_21_3"))
  implementation(project(":openinvadapterpaper1_21_1"))
  implementation(project(":openinvadapterspigot", configuration = "shadow"))
  implementation(libs.planarwrappers)
}

tasks.processResources {
  expand("version" to version)
}

tasks.jar {
  manifest.attributes("paperweight-mappings-namespace" to "mojang")
}

tasks.shadowJar {
  dependsOn(":openinvadapterspigot:reobf")
  minimize {
    exclude(":openinv**")
  }
}

tasks.register<Copy>("distributePlugin") {
  into(rootProject.layout.projectDirectory.dir("dist"))
  from(tasks.shadowJar)
  rename("openinvplugin.*\\.jar", "OpenInv.jar")
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
  dependsOn(tasks.named("distributePlugin"))
}
