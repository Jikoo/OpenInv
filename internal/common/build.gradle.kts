plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
}

dependencies {
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))

  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}
