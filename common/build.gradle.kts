repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  compileOnly(libs.spigotapi)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

dependencies {
  implementation(project(":openinvapi"))
  compileOnly(libs.slf4j.api)
}
