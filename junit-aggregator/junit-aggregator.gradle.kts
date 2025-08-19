plugins {
    id("junitbuild.java-library-conventions")
    id("junitbuild.java-nullability-conventions")
}

description = "JUnit Aggregator"

dependencies {
    api(projects.junitJupiter)
    compileOnlyApi(projects.junitJupiterEngine)
    implementation(projects.junitPlatformLauncher)
    implementation(projects.junitPlatformConsole)
}
