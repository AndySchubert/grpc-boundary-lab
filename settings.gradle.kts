rootProject.name = "grpc-boundary-lab"

include("proto")
project(":proto").projectDir = file("src/proto")
include("backend")
project(":backend").projectDir = file("src/backend")
include("gateway")
project(":gateway").projectDir = file("src/gateway")
include("loadgen")
project(":loadgen").projectDir = file("src/loadgen")
