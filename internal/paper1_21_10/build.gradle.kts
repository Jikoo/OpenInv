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
  implementation(project(":openinvadapterpaper26_1"))

  paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
}
