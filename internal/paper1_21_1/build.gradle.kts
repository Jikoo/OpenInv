plugins {
  alias(libs.plugins.paperweight)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

dependencies {
  implementation(project(":openinvapi")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }
  implementation(project(":openinvcommon")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }
  implementation(project(":openinvadaptercommon"))
  implementation(project(":openinvadapterpaper1_21_8"))
  implementation(project(":openinvadapterpaper1_21_5"))
  implementation(project(":openinvadapterpaper1_21_4"))
  implementation(project(":openinvadapterpaper1_21_3"))

  paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}
