plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
  alias(libs.plugins.shadow)
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
  implementation(project(":openinvadaptercommon", configuration = "reobf"))

  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks.jar {
  dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
}
