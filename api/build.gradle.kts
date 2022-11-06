applyCommonConfiguration()

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    "compileOnly"("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
}
