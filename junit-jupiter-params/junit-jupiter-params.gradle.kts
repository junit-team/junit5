plugins {
	`kotlin-library-conventions`
	`shadow-conventions`
	`testing-conventions`
}

description = "JUnit Jupiter Params"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.jupiter.api)

	shadowed(libs.univocity.parsers)

	testImplementation(projects.platform.testkit)
	testImplementation(projects.jupiter.engine)
	testImplementation(projects.platform.launcher)
	testImplementation(projects.platform.runner)

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
