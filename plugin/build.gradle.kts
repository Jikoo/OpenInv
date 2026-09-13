import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  alias(libs.plugins.shadow)
}

repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
  maven("https://jitpack.io")
}

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
  compileOnly(libs.spigotapi)
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))
  implementation(project(":openinvadapterpaper26_2")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper26_1")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_11")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterpaper1_21_10")) {
    exclude(group = "io.papermc.paper", module = "dev-bundle")
  }
  implementation(project(":openinvadapterspigot26_2")) {
    exclude(group = "org.spigotmc", module = "spigot")
  }
  implementation(project(":openinvadapterspigot26_1")) {
    exclude(group = "org.spigotmc", module = "spigot")
  }
  implementation(libs.planarwrappers)
  implementation(libs.folia.scheduler.wrapper)
  compileOnly(libs.sqlite.jdbc)

  testImplementation(rootProject.libs.hamcrest)
  testImplementation(libs.mockito.core)
  mockitoAgent(libs.mockito.core) { isTransitive = false }
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

tasks.withType<Test>().configureEach {
  // Use as many cores as possible to run tests.
  maxParallelForks = Runtime.getRuntime().availableProcessors()
  // As Bukkit is very heavily statically initialized, don't reuse forks.
  forkEvery = 1
  jvmArgs("-Xshare:off", "-javaagent:${mockitoAgent.asPath}")
  testLogging {
    showStackTraces = true
    exceptionFormat = TestExceptionFormat.FULL
    events(TestLogEvent.STANDARD_OUT)
  }
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter(libs.junit.jupiter.get().version!!)
    }
  }
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
