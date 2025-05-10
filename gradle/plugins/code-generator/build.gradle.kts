plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("junitbuild.base:code-generator-model")
	implementation("junitbuild.base:dsl-extensions")
	implementation(projects.common)
	implementation(libs.jackson.dataformat.yaml)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.jte)
}
