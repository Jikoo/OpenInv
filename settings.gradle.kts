rootProject.name = "openinvparent"

include(":openinvapi")
project(":openinvapi").projectDir = file("api")

if (!java.lang.Boolean.getBoolean("jitpack")) {
  val addons = listOf(
    "togglepersist"
  )
  for (addon in addons) {
    include(":addon$addon")
    val proj = project(":addon$addon")
    proj.projectDir = file("addon/$addon")
    proj.name = "openinv$addon"
  }

  include(":openinvcommon")
  project(":openinvcommon").projectDir = file("common")

  val internals = listOf(
    "common",
    "paper1_21_5",
    "paper1_21_4",
    "paper1_21_3",
    "paper1_21_1",
    "spigot"
  )
  for (internal in internals) {
    include(":openinvadapter$internal")
    project(":openinvadapter$internal").projectDir = file("internal/$internal")
  }

  include(":resource-pack")

  include(":plugin")
  project(":plugin").name = "openinvplugin"
}
