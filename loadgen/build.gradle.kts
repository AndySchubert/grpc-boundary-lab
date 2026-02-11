plugins {
    application
}

dependencies {
    implementation(project(":proto"))
    implementation("io.grpc:grpc-netty-shaded:1.60.0")
    implementation("io.grpc:grpc-protobuf:1.60.0")
    implementation("io.grpc:grpc-stub:1.60.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

application {
    mainClass.set("lab.loadgen.LoadgenMain")
}
