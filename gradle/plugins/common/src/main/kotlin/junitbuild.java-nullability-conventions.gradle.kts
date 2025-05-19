import junitbuild.extensions.dependencyFromLibs
import net.ltgt.gradle.errorprone.errorprone

plugins {
	`java-library`
	id("net.ltgt.errorprone")
	id("net.ltgt.nullaway")
}

dependencies {
	errorprone(dependencyFromLibs("errorProne-core"))
	errorprone(dependencyFromLibs("nullaway"))
}

nullaway {
	onlyNullMarked = true
}

tasks.withType<JavaCompile>().configureEach {
	options.errorprone {
		disableAllChecks = true
		enable("NullAway")
	}
}
