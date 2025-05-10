plugins {
	id("junitbuild.java-library-conventions")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)
}

tasks.compileJava {
	options.compilerArgs.add("-Xlint:-module") // due to qualified exports
}
