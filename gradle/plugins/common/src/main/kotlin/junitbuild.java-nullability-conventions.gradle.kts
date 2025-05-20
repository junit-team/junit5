import junitbuild.extensions.dependencyFromLibs
import junitbuild.extensions.javaModuleName
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
	annotatedPackages.add(javaModuleName)
}

tasks.withType<JavaCompile>().configureEach {
	options.errorprone {
		disableAllChecks = true
		enable("NullAway")
	}
}
