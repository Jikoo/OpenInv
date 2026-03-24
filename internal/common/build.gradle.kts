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
