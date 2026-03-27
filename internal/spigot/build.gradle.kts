import com.github.jikoo.openinv.SpigotDependencyExtension
import com.github.jikoo.openinv.SpigotSetup

plugins {
  `openinv-base`
  alias(libs.plugins.shadow)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

apply<SpigotSetup>()

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
  extensions.getByType(SpigotDependencyExtension::class.java).version = "26.1-R0.1-SNAPSHOT"
  compileOnly("com.mojang:logging:1.6.11")
  compileOnly("com.mojang:brigadier:1.3.10")
  compileOnly("com.mojang:datafixerupper:9.0.19")
  compileOnly("com.mojang:authlib:7.0.62")

  compileOnly(project(":openinvapi"))
  compileOnly(project(":openinvcommon"))
}
