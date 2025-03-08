plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
  implementation(variantOf(libs.findLibrary("specialsource").orElseThrow()) { classifier("shaded") })
}
