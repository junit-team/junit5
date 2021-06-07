plugins {
	`kotlin-library-conventions`
	`shadow-conventions`
	`testing-conventions`
}

description = "JUnit Jupiter Params"

dependencies {
	api(platform(projects.junitBom))
	api(libs.apiguardian)
	api(projects.junitJupiterApi)

	shadowed(libs.univocity.parsers)

	testImplementation(projects.junitPlatformTestkit)
	testImplementation(projects.junitJupiterEngine)
	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformRunner)

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
