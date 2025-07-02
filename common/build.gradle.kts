plugins {
  `openinv-base`
}

dependencies {
  implementation(project(":openinvapi"))
  compileOnly(libs.slf4j.api)
}
