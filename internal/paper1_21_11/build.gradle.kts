import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

  paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

val spigot = tasks.register<ShadowJar>("spigotRelocations") {
  dependsOn(tasks.jar)
  from(sourceSets.main.get().output)
  relocate("com.lishid.openinv.internal.paper1_21_11", "com.lishid.openinv.internal.reobf")
  relocate("org.bukkit.craftbukkit", "org.bukkit.craftbukkit.${rootProject.extra["craftbukkitPackage"]}")
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
