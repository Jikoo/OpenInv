plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  val libs = versionCatalogs.named("libs")
  val shadow = libs.findPlugin("shadow").orElseThrow().get()
  implementation("${shadow.pluginId}:shadow-gradle-plugin:${shadow.version}")
}
