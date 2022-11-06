rootProject.name = "OpenInv"

include("adapters:v1_18_R2")
include("adapters:v1_19_R1")

include("OpenInvAPI")
project(":OpenInvAPI").projectDir = file("api")

include("OpenInvPlugin")
project(":OpenInvPlugin").projectDir = file("plugin")

