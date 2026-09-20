import com.github.jikoo.openinv.SpigotDependencyExtension
import com.github.jikoo.openinv.SpigotSetup

plugins {
  alias(libs.plugins.shadow)
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

apply<SpigotSetup>()

dependencies {
  compileOnly(libs.spigotapi)
  extensions.getByType(SpigotDependencyExtension::class.java).version = "26.2-R0.1-SNAPSHOT"
  compileOnly("com.mojang:logging:1.7.12")
  compileOnly("com.mojang:brigadier:1.3.10")
  compileOnly("com.mojang:datafixerupper:10.0.21")
  compileOnly("com.mojang:authlib:9.0.75")

  compileOnly(project(":openinvapi"))
  compileOnly(project(":openinvcommon"))

  compileOnly(project(":openinvadapterspigot26_3")) {
    exclude(group = "org.spigotmc", module = "spigot")
  }
}
