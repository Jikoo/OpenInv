import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
}

//tasks {
//  withType<JavaCompile> {
//    // OpenPlayer unchecked warning is due to superclass' messy inheritance and legacy methods.
//    options.compilerArgs.add("-Xlint:unchecked")
//    // PlayerManager uses "deprecated" method matching vanilla to support legacy save data.
//    // While vanilla still feels that it is appropriate to use in the load process, we will too.
//    options.compilerArgs.add("-Xlint:deprecation")
//  }
//}

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
