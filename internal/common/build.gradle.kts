import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

val spigot = tasks.register<ShadowJar>("spigotRelocations") {
  dependsOn(tasks.jar)
  from(sourceSets.main.get().output)
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
  // TODO pass from Spigot adapter somehow. Can tasks be added by another project? Can we fetch a global config?
  relocate("org.bukkit.craftbukkit", "org.bukkit.craftbukkit.v1_21_R3")
  archiveClassifier = "spigot"
}

configurations {
  consumable("spigotRelocated") {
    attributes {
      attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
      attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
      attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
      attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
    }
  }
}

artifacts {
  add("spigotRelocated", spigot)
}
