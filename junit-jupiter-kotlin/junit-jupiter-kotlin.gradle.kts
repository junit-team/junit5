plugins {
	id("junitbuild.kotlin-library-conventions")
}

description = "JUnit Jupiter Kotlin support"

dependencies {
	implementation(projects.junitJupiterEngine)
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	implementation(libs.kotlinx.coroutines)
}
