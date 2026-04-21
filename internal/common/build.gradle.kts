import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
  `openinv-base`
  alias(libs.plugins.paperweight)
}

// Paper 26.1's dev bundle requires a JDK 25 toolchain to read NMS class files,
// but we target JDK 21 bytecode (openinv-base's default via options.release) so
// the legacy paper1_21_X adapters (still on a JDK 21 toolchain) can consume
// this module's output. common's source stays within the Java 21 language
// level; there are no Java 22+ features in use here.
java {
  toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// Paper 26.1's paper-api variant advertises jvm.version=25. Our outgoing target
// is 21 (so legacy adapters can consume our jar), but our compile/runtime
// classpaths are resolved by a JDK 25 toolchain — tell Gradle we're willing to
// consume JVM 25 libs here.
configurations.matching { it.name.endsWith("Classpath") }.configureEach {
  attributes {
    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
  }
}

configurations.all {
  resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
    val paper = candidates.firstOrNull {
      it.id.let { id ->
        id is ModuleComponentIdentifier && id.module == "paper-api"
      }
    }
    if (paper != null) {
      select(paper)
    }
    because("module is written for Paper servers")
  }
}

dependencies {
  implementation(project(":openinvapi"))
  implementation(project(":openinvcommon"))

  paperweight.paperDevBundle("26.1.2.build.+")
}

// Paper 26.1+ ships unobfuscated server jars and dev bundles carry no reobf
// mappings, so paperweight's reobfJar task has nothing to do. Skip it. The
// plugin module consumes this adapter via its plain jar output.
tasks.named("reobfJar") {
  enabled = false
}

// Note: Spigot reobfuscation is no longer viable for 26.1+. The
// spigotRelocations task previously hosted here has moved to
// :openinvadapterpaper1_21_11 (the last reobf-capable version).
