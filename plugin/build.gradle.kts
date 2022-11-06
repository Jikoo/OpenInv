import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyCommonConfiguration()
apply(plugin = "com.github.johnrengelman.shadow")

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

val adapters = configurations.create("adapters") {
    description = "Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(io.papermc.paperweight.userdev.attribute.Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(io.papermc.paperweight.userdev.attribute.Obfuscation.OBFUSCATED))
    }
}

dependencies {
    "api"(project(":OpenInvAPI"))

    "compileOnly"("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")

    project.project(":adapters").subprojects.forEach {
        "adapters"(project(it.path))
    }
}

tasks.named<Copy>("processResources") {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":adapters").subprojects.map { it.tasks.named("assemble") })
    from(Callable {
        adapters.resolve()
            .map { f ->
                zipTree(f).matching {
                    exclude("META-INF/")
                }
            }
    })
    dependencies {
        include(project(":OpenInvAPI"))
    }
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/*")
    minimize()
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
