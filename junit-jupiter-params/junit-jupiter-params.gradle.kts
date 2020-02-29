plugins {
	`kotlin-library-conventions`
	`shadow-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Params"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-jupiter-api"))

	shadowed(platform(project(":dependencies")))
	shadowed("com.univocity:univocity-parsers")

	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))

	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks {
	shadowJar {
		relocate("com.univocity", "org.junit.jupiter.params.shadow.com.univocity")
		from(projectDir) {
			include("LICENSE-univocity-parsers.md")
			into("META-INF")
		}
	}
}
