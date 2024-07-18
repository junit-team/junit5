plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.junit4-compatibility")
	id("junitbuild.testing-conventions")
	`java-test-fixtures`
	groovy
}

description = "JUnit Vintage Engine"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)
	api(libs.junit4)

	compileOnlyApi(libs.apiguardian)

	testFixturesApi(platform(libs.groovy2.bom))
	testFixturesApi(libs.spock1)
	testFixturesImplementation(projects.junitPlatformRunner)

	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformSuiteEngine)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitJupiterApi))

	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	compileTestFixturesGroovy {
		javaLauncher = project.javaToolchains.launcherFor {
			// Groovy 2.x (used for Spock tests) does not support current JDKs
			languageVersion = JavaLanguageVersion.of(8)
		}
	}
	jar {
		bundle {
			val junit4Min = libs.versions.junit4Min.get()
			val platformVersion: String by rootProject.extra
			val version = project.version
			bnd("""
				# Import JUnit4 packages with a version
				Import-Package: \
					${extra["importAPIGuardian"]},\
					junit.runner;version="[${junit4Min},5)",\
					org.junit;version="[${junit4Min},5)",\
					org.junit.experimental.categories;version="[${junit4Min},5)",\
					org.junit.internal.builders;version="[${junit4Min},5)",\
					org.junit.platform.commons.logging;status=INTERNAL,\
					org.junit.runner.*;version="[${junit4Min},5)",\
					org.junit.runners.model;version="[${junit4Min},5)",\
					*

				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-vintage';\
						version:Version="${'$'}{version_cleanup;$version}"
				Require-Capability:\
					org.junit.platform.launcher;\
						filter:='(&(org.junit.platform.launcher=junit-platform-launcher)(version>=${'$'}{version_cleanup;$platformVersion})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;$platformVersion}})))';\
						effective:=active
			""")
		}
	}
	val testWithoutJUnit4 by registering(Test::class) {
		val test by testing.suites.existing(JvmTestSuite::class)
		(options as JUnitPlatformOptions).apply {
			includeTags("missing-junit4")
		}
		setIncludes(listOf("**/JUnit4VersionCheckTests.class"))
		testClassesDirs = files(test.map { it.sources.output.classesDirs })
		classpath = files(test.map { it.sources.runtimeClasspath }).filter {
			!it.name.startsWith("junit-4")
		}
	}
	withType<Test>().named { it != testWithoutJUnit4.name }.configureEach {
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
