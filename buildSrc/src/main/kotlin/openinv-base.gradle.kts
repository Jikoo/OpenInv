import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.dependencies

plugins {
  `java-library`
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  val libs = versionCatalogs.named("libs")
  compileOnly(libs.findLibrary("annotations").orElseThrow())
  compileOnly(libs.findLibrary("spigotapi").orElseThrow())
}

tasks {
  withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = Charsets.UTF_8.name()
  }
  withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name()
  }
}
