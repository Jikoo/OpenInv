import org.gradle.api.internal.attributes.DefaultCompatibilityRuleChain

plugins {
  alias(libs.plugins.paperweight)
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21
}

class ToolchainJvmVersion: AttributeCompatibilityRule<Int> {
  override fun execute(details: CompatibilityCheckDetails<Int>) {
    if (details.consumerValue == details.producerValue) {
      details.compatible()
      return
    }
    if (details.producerValue == null) {
      details.incompatible()
      return
    }
    if (25 >= details.producerValue!!) {
      details.compatible()
    } else {
      details.incompatible()
    }
  }
}

dependencies {
  attributesSchema {
    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE) {
      // Gradle thinks that depending on a library with a certain language level requires
      // any project that depends on it to also require that language level no matter what.
      // This is a bit obnoxious when we're specifically adding compatiblity patches to shared common code.
      // Realistically Gradle is correct and the chain should be reversed - the oldest, least common
      // denominator should be targeted and patches should be slapped on top as necessary.
      // However, I don't want to spend a week restructuring the project to cut a release.
      // Hack out Gradle's rules and replace them with our own less-restrictive version.
      val rules = compatibilityRules as DefaultCompatibilityRuleChain
      rules.rules.clear()

      compatibilityRules.add(ToolchainJvmVersion::class.java)
    }
  }

  implementation(project(":openinvapi")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }
  implementation(project(":openinvcommon")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }

  paperweight.paperDevBundle("26.1.1.build.15-alpha")
}

tasks.reobfJar {
  enabled = false
}

configurations.reobf.get().outgoing.artifacts.clear()
