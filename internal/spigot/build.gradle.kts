import com.github.jikoo.openinv.SpigotDependencyExtension
import com.github.jikoo.openinv.SpigotReobf
import com.github.jikoo.openinv.SpigotSetup

plugins {
  `openinv-base`
  alias(libs.plugins.shadow)
}

apply<SpigotSetup>()
apply<SpigotReobf>()

val spigotVer = "1.21.11-R0.2-SNAPSHOT"
// Used by the paper1_21_11 adapter to relocate Craftbukkit classes to a versioned package.
rootProject.extra["craftbukkitPackage"] = "v1_21_R7"

configurations.all {
  resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
    val spigot = candidates.firstOrNull {
      it.id.let { id ->
        id is ModuleComponentIdentifier && id.module == "spigot-api"
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

  // Reduce duplicate code by lightly remapping the paper1_21_11 adapter. Paper 26.1+
  // has no Spigot reobfuscation path, so 1.21.11 is the last reobf-capable version.
  implementation(project(":openinvadapterpaper1_21_11", configuration = "spigotRelocated"))
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.paper1_21_11", "com.lishid.openinv.internal.reobf")
}
