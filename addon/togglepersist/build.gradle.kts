repositories {
  mavenCentral()
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  compileOnly(libs.spigotapi)
  implementation(project(":openinvapi"))
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

tasks.processResources {
  expand("version" to version)
}

tasks.register<Copy>("distributeAddons") {
  into(rootProject.layout.projectDirectory.dir("dist"))
  from(tasks.jar)
  rename("openinvtogglepersist.*\\.jar", "OITogglePersist.jar")
}

tasks.assemble {
  dependsOn(tasks.named("distributeAddons"))
}
