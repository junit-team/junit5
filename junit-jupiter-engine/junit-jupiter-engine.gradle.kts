import aQute.bnd.gradle.BundleTaskConvention
import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`kotlin-library-conventions`
	`testing-conventions`
	groovy
}

description = "JUnit Jupiter Engine"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)

	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformRunner)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitPlatformCommons))
	testImplementation(kotlin("stdlib"))
	testImplementation(libs.kotlinx.coroutines)
	testImplementation(libs.groovy3)
}

tasks {
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
	}
}

tasks {
	jar {
		withConvention(BundleTaskConvention::class) {
			bnd("""
				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-jupiter';\
						version:Version="${'$'}{version_cleanup;${project.version}}"
			""")
		}
	}
}
