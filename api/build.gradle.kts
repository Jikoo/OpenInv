plugins {
  `maven-publish`
}

repositories {
  mavenCentral()
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

publishing {
  publications {
    create<MavenPublication>("jitpack") {
      groupId = "com.github.Jikoo.OpenInv"
      artifactId = "openinvapi"
      from(components["java"])
    }
  }
}
