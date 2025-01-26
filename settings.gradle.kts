rootProject.name = "openinvparent"

include(":openinvapi")
project(":openinvapi").projectDir = file("api")

// TODO addons
//val addons = listOf(
//  "togglepersist"
//)
//for (addon in addons) {
//  include("addon:$addon")
//  project(":$addon").name = "openinv$addon"
//}

include(":openinvcommon")
project(":openinvcommon").projectDir = file("common")

val internals = listOf(
  "common",
  "spigot"
)
for (internal in internals) {
  include(":openinvadapter$internal")
  val proj = project(":openinvadapter$internal")
  proj.projectDir = file("internal/$internal")
  proj.name = "openinvadapter$internal"
}

include(":resource-pack")

include(":plugin")
project(":plugin").name = "openinvplugin"
