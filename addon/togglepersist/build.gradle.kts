plugins {
  `openinv-base`
}

dependencies {
  implementation(project(":openinvapi"))
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
