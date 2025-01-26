plugins {
  `base`
}

tasks.register<Zip>("buildResourcePack") {
  archiveFileName = "openinv-legibility-pack.zip"
  destinationDirectory = rootProject.layout.projectDirectory.dir("dist")

  from("openinv-legibility-pack")
  with (copySpec {
    include("**/*.json", "**/*.png", "pack.mcmeta")
  })
}

tasks.assemble {
  dependsOn(tasks.named("buildResourcePack"))
}
