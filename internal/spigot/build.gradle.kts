plugins {
  `remap-spigot`
  `openinv-base`
  alias(libs.plugins.shadow)
}

repositories {
  mavenLocal()
}

configurations.all {
  resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
    val spigot = candidates.firstOrNull {
      it.id.let {
        id -> id is ModuleComponentIdentifier && id.module == "spigot-api"
      }
    }
    if (spigot != null) {
      select(spigot)
    }
    because("module is written for Spigot servers")
  }
}

dependencies {
  compileOnly(libs.spigotapi)
  compileOnly(variantOf(libs.spigot) { classifier("remapped-mojang") })

  compileOnly(project(":openinvapi"))
  compileOnly(project(":openinvcommon"))

  // Reduce duplicate code by lightly remapping common adapter.
  implementation(project(":openinvadaptercommon", configuration = "spigotRelocated"))
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
}

// TODO this appears to be a deprecated way to do things
//   may want to just move all helper methods here.
tasks.register<Remap_spigot_gradle.RemapTask>("reobf") {
  notCompatibleWithConfigurationCache("gradle is hard")
  dependsOn(tasks.shadowJar)
  inputs.files(tasks.shadowJar.get().outputs.files.files)
  spigotVersion = libs.spigot.get().version.toString()
}
