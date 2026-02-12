import org.gradle.api.plugins.JavaPluginExtension

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.apply("java")

    extensions.configure<JavaPluginExtension>("java") {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
