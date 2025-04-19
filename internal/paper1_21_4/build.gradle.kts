plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
}

configurations.all {
  resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
    val paper = candidates.firstOrNull {
      it.id.let {
        id -> id is ModuleComponentIdentifier && id.module == "paper-api"
      }
    }
    if (paper != null) {
      select(paper)
    }
    because("module is written for Paper servers")
  }
}

dependencies {
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))
  implementation(project(":openinvadaptercommon"))

  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}
