import aQute.bnd.gradle.BundleTaskConvention;

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
	`java-test-fixtures`
	groovy
}

description = "JUnit Vintage Engine"

dependencies {
	api(platform(projects.bom))
	api(libs.apiguardian)
	api(projects.platform.engine)
	api(libs.junit4)

	testFixturesApi(platform(libs.groovy2.bom))
	testFixturesApi(libs.spock1)
	testFixturesImplementation(projects.platform.runner)

	testImplementation(projects.platform.launcher)
	testImplementation(projects.platform.runner)
	testImplementation(projects.platform.testkit)
}

tasks {
	compileTestFixturesGroovy {
		javaLauncher.set(project.the<JavaToolchainService>().launcherFor {
			// Groovy 2.x (used for Spock tests) does not support JDK 16
			languageVersion.set(JavaLanguageVersion.of(11))
		})
	}
	jar {
		withConvention(BundleTaskConvention::class) {
			val junit4Min = libs.versions.junit4Min.get()
			bnd("""
				# Import JUnit4 packages with a version
				Import-Package: \
					!org.apiguardian.api,\
					junit.runner;version="[${junit4Min},5)",\
					org.junit;version="[${junit4Min},5)",\
					org.junit.experimental.categories;version="[${junit4Min},5)",\
					org.junit.internal.builders;version="[${junit4Min},5)",\
					org.junit.platform.commons.logging;status=INTERNAL,\
					org.junit.runner.*;version="[${junit4Min},5)",\
					org.junit.runners.model;version="[${junit4Min},5)",\
					*
			""")
		}
	}
	val testWithoutJUnit4 by registering(Test::class) {
		(options as JUnitPlatformOptions).apply {
			includeTags("missing-junit4")
		}
		setIncludes(listOf("**/JUnit4VersionCheckTests.class"))
		classpath = classpath.filter {
			!it.name.startsWith("junit-4")
		}
	}
	withType<Test>().matching { it.name != testWithoutJUnit4.name }.configureEach {
		(options as JUnitPlatformOptions).apply {
			excludeTags("missing-junit4")
		}
	}
	withType<Test>().configureEach {
		// Workaround for Groovy 2.5
		jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
		jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
	}
	check {
		dependsOn(testWithoutJUnit4)
	}
}
