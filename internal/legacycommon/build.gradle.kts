plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
}

configurations.all {
  resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
    val paper = candidates.firstOrNull {
      it.id.let { id ->
        id is ModuleComponentIdentifier && id.module == "paper-api"
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

  // Pin to the latest pre-26.1 dev bundle so this module compiles against the
  // NMS ClickType that still exists on 1.21.1-1.21.10 servers. NMS ClickType
  // was renamed to ContainerInput starting in 26.1, so common's clicked()
  // override is no longer an override on 1.21.X servers. This shim restores
  // the view-only QUICK_CRAFT re-sync hook for the legacy adapters.
  paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
}

// This module is a compile-time shim consumed by the older paper1_21_X
// adapters; it never ships on its own, so skip reobfJar to avoid extra work.
tasks.named("reobfJar") {
  enabled = false
}
