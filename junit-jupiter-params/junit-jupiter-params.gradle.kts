plugins {
	`kotlin-library-conventions`
	`shadow-conventions`
	id("de.jjohannes.extra-java-module-info") version "0.1"
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

extraJavaModuleInfo {
	automaticModule("univocity-parsers-${versions["univocity-parsers"]}.jar", "univocity-parsers")
	automaticModule("kotlin-stdlib-1.3.72.jar", "kotlin-stdlib")
	automaticModule("kotlin-stdlib-common-1.3.72.jar", "kotlin-stdlib-common")
	automaticModule("annotations-13.0.jar", "jetbrains-annotations")
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
