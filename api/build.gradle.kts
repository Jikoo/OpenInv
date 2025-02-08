plugins {
  `openinv-base`
  `maven-publish`
}

publishing {
  publications {
    create<MavenPublication>("jitpack") {
      groupId = "com.github.Jikoo.OpenInv"
      artifactId = "openinvapi"
    }
  }
}
