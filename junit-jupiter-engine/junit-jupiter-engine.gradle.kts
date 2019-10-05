plugins {
	`kotlin-library-conventions`
	groovy
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	testImplementation("org.codehaus.groovy:groovy-all")
}
