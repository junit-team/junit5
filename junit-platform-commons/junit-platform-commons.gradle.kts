plugins {
	id("junitbuild.kotlin-library-conventions")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)

	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.kotlinx.coroutines)
}

tasks.compileJava {
	options.compilerArgs.add("-Xlint:-module") // due to qualified exports
}
