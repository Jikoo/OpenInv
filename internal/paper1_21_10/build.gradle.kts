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
  implementation(project(":openinvadapterpaper26_1")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_11")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }

  paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
}
