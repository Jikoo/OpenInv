plugins {
  `openinv-base`
  alias(libs.plugins.shadow)
}

repositories {
  mavenLocal()
}

val spigotVer = "1.21.4-R0.1-SNAPSHOT"
rootProject.extra["spigotVersion"] = spigotVer
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

val spigotRemap = configurations.create("spigotRemap")

dependencies {
  spigotRemap("net.md-5:SpecialSource:1.11.4:shaded")
  compileOnly(libs.spigotapi)
  compileOnly(create("org.spigotmc", "spigot", spigotVer, classifier = "remapped-mojang"))

  compileOnly(project(":openinvapi"))
  compileOnly(project(":openinvcommon"))

  // Reduce duplicate code by lightly remapping common adapter.
  implementation(project(":openinvadaptercommon", configuration = "spigotRelocated"))
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
}

val reobfTask = tasks.register<ReobfTask>("reobfTask") {
  notCompatibleWithConfigurationCache("gradle is hard")
  dependsOn(tasks.shadowJar)
  inputFile.value(tasks.shadowJar.get().archiveFile.get())
  spigotVersion.value(spigotVer)
}

configurations {
  consumable("reobf") {
    attributes {
      attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
      attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
      attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
      attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
  }
}

artifacts {
  add("reobf", reobfTask)
}
