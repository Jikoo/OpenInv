import com.github.jikoo.openinv.SpigotDependencyExtension
import com.github.jikoo.openinv.SpigotReobf
import com.github.jikoo.openinv.SpigotSetup

plugins {
  `openinv-base`
  alias(libs.plugins.shadow)
}

apply<SpigotSetup>()
apply<SpigotReobf>()

val spigotVer = "1.21.4-R0.1-SNAPSHOT"
// Used by common adapter to relocate Craftbukkit classes to a versioned package.
rootProject.extra["craftbukkitPackage"] = "v1_21_R3"

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
  extensions.getByType(SpigotDependencyExtension::class.java).version = spigotVer

  compileOnly(project(":openinvapi"))
  compileOnly(project(":openinvcommon"))

  // Reduce duplicate code by lightly remapping common adapter.
  implementation(project(":openinvadaptercommon", configuration = "spigotRelocated"))
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
}
