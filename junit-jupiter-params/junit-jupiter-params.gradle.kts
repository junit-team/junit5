plugins {
	`kotlin-library-conventions`
	`shadow-conventions`
	`testing-conventions`
}

description = "JUnit Jupiter Params"

dependencies {
	api(platform(project(":junit-bom")))
	api(libs.apiguardian)
	api(project(":junit-jupiter-api"))

	shadowed(libs.univocity.parsers)

	testImplementation(project(":junit-platform-testkit"))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))

	compileOnly(kotlin("stdlib"))
	testImplementation(kotlin("stdlib"))
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
