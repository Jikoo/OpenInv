plugins {
  alias(libs.plugins.shadow)
}

repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
  maven("https://jitpack.io")
}

dependencies {
  compileOnly(libs.spigotapi)
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))
  implementation(project(":openinvadapterpaper26_1")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_11")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_10")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_8")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterspigot")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(libs.planarwrappers)
  implementation(libs.folia.scheduler.wrapper)
  compileOnly(libs.sqlite.jdbc)
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

tasks.processResources {
  expand(
    mutableMapOf(
      "version" to version,
      "sqlite" to libs.sqlite.jdbc.get().version
    )
  )
}

tasks.jar {
  manifest.attributes("paperweight-mappings-namespace" to "mojang")
}

tasks.shadowJar {
  relocate("me.nahu.scheduler.wrapper", "com.github.jikoo.openinv.lib.nahu.scheduler-wrapper")
  relocate("com.github.jikoo.planarwrappers", "com.github.jikoo.openinv.lib.planarwrappers")
  minimize {
    exclude(":openinv**")
    exclude(dependency(libs.folia.scheduler.wrapper.get()))
  }
}

tasks.register<Copy>("distributePlugin") {
  into(rootProject.layout.projectDirectory.dir("dist"))
  from(tasks.shadowJar)
  rename("openinvplugin.*\\.jar", "OpenInv.jar")
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
  dependsOn(tasks.named("distributePlugin"))
}
