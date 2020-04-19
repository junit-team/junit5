import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`java-test-fixtures`
	groovy
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Vintage Engine"

dependencies {
	internal(platform(project(":dependencies")))

	api(platform(project(":junit-bom")))
	api("org.apiguardian:apiguardian-api")
	api(project(":junit-platform-engine"))
	api("junit:junit")

	testFixturesApi("org.spockframework:spock-core")
	testFixturesApi(project(":junit-jupiter-api"))
	testFixturesImplementation(project(":junit-platform-runner"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "org.codehaus.groovy") {
			useVersion("2.5.11")
			because("Spock is not yet compatible with Groovy 3.x")
		}
	}
}

tasks {
	jar {
		withConvention(BundleTaskConvention::class) {
			bnd("""
				# Import JUnit4 packages with a version
				Import-Package: \
					!org.apiguardian.api,\
					junit.runner;version="[${versions.junit4Min},5)",\
					org.junit;version="[${versions.junit4Min},5)",\
					org.junit.experimental.categories;version="[${versions.junit4Min},5)",\
					org.junit.internal.builders;version="[${versions.junit4Min},5)",\
					org.junit.platform.commons.logging;status=INTERNAL,\
					org.junit.runner.*;version="[${versions.junit4Min},5)",\
					org.junit.runners.model;version="[${versions.junit4Min},5)",\
					*
			""")
		}
	}
	val testWithoutJUnit4 by registering(Test::class) {
		(options as JUnitPlatformOptions).apply {
			includeTags("missing-junit4")
		}
		filter {
			includeTestsMatching("org.junit.vintage.engine.JUnit4VersionCheckTests")
		}
		classpath = classpath.filter {
			!it.name.startsWith("junit-4")
		}
	}
	withType<Test>().matching { it.name != testWithoutJUnit4.name }.configureEach {
		(options as JUnitPlatformOptions).apply {
			excludeTags("missing-junit4")
		}
	}
	check {
		dependsOn(testWithoutJUnit4)
	}
}
