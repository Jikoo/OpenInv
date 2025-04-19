import com.github.jikoo.openinv.SpigotReobf

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
  implementation(project(":openinvadapterpaper1_21_4"))
  implementation(project(":openinvadapterpaper1_21_3"))
  implementation(project(":openinvadapterpaper1_21_1"))
  implementation(project(":openinvadapterspigot", configuration = SpigotReobf.ARTIFACT_CONFIG))
  implementation(libs.planarwrappers)
  implementation(libs.folia.scheduler.wrapper)
}

tasks.processResources {
  expand("version" to version)
}

tasks.jar {
  manifest.attributes("paperweight-mappings-namespace" to "mojang")
}

tasks.shadowJar {
  relocate("me.nahu.scheduler.wrapper", "com.github.jikoo.openinv.lib.nahu.scheduler-wrapper")
  relocate("com.github.jikoo.planarwrappers", "com.github.jikoo.openinv.lib.planarwrappers")
  minimize {
    exclude(":openinv**")
    exclude(dependency(libs.folia.scheduler.wrapper.get()))
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
