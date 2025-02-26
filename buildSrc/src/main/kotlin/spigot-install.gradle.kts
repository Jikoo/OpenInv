import java.io.File
import java.nio.file.Paths

plugins.apply("java")

// BuildTools installs Spigot to the local Maven repo.
val mvnLocal = repositories.mavenLocal()

dependencies.extensions.create(
  "spigot",
  SpigotDependencyExtension::class.java,
  dependencies,
  objects.property(File::class.java).convention(Paths.get(mvnLocal.url).toFile()),
  layout.buildDirectory.dir("tmp/buildtools")
)
