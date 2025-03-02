package com.github.jikoo.openinv

import org.gradle.api.model.ObjectFactory
import org.gradle.jvm.toolchain.JavaToolchainSpec

abstract class SpigotDependencyExtension (
  objects: ObjectFactory
) {

  val version = objects.property(String::class.java)
  val revision = objects.property(String::class.java)
    .convention(version.map {
      it.replace("-R\\d+\\.\\d+-SNAPSHOT".toRegex(), "")
    })
  val configuration = objects.property(String::class.java)
  val classifier = objects.property(String::class.java).convention("remapped-mojang")
  val ext = objects.property(String::class.java)
  val java = objects.property(JavaToolchainSpec::class.java)
  val ignoreCached = objects.property(Boolean::class.java).convention(false)

}
