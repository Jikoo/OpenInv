plugins {
  alias(libs.plugins.paperweight)
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

// Paper is compiled with Java 25, so we have to use a Java 25 toolchain.
java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// To minimize duplicate code, the adapter for 1.21.11 is built off of this module, so we need to target Java 21.
tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

// As a result, we have to specifically tell Gradle it's okay if our dependencies require Java 25,
// because secretly sometimes they don't!
configurations.matching { it.isCanBeResolved && it.name.endsWith("Classpath") }.all {
  attributes {
    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
  }
}

dependencies {
  implementation(project(":openinvapi")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }
  implementation(project(":openinvcommon")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }
  api(project(":openinvadapterpaper26_2")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }

  paperweight.paperDevBundle("26.1.2.build.+")
}

tasks.reobfJar {
  enabled = false
}

configurations.reobf.get().outgoing.artifacts.clear()
