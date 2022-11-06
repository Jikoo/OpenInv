
applyCommonConfiguration()
apply(plugin = "io.papermc.paperweight.userdev")

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperDevBundle("1.19.2-R0.1-20221105.185610-143")

    "implementation"(project(":OpenInvPlugin"))
    "implementation"(project(":OpenInvAPI"))
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
