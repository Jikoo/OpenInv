plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(project(":openinvadaptercommon", configuration = "reobf"))

  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks.jar {
  dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
  relocate("com.lishid.openinv.internal.common", "com.lishid.openinv.internal.reobf")
}
