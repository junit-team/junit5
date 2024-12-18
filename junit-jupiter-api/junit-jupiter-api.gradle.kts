plugins {
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.code-generator")
	id("junitbuild.native-image-properties")
	`java-test-fixtures`
}

description = "JUnit Jupiter API"

dependencies {
	api(platform(projects.junitBom))
	api(libs.opentest4j)
	api(projects.junitPlatformCommons)

	compileOnlyApi(libs.apiguardian)

	compileOnly(kotlin("stdlib"))

	testFixturesImplementation(libs.assertj)

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.jupiter.api.DisplayNameGenerator\$Standard",
		"org.junit.jupiter.api.TestInstance\$Lifecycle",
		"org.junit.jupiter.api.condition.OS",
		"org.junit.jupiter.api.extension.ConditionEvaluationResult",
	)
}

tasks {
	jar {
		bundle {
			val version = project.version
			bnd("""
				Require-Capability:\
					org.junit.platform.engine;\
						filter:='(&(org.junit.platform.engine=junit-jupiter)(version>=${'$'}{version_cleanup;${version}})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;${version}}})))';\
						effective:=active
			""")
		}
	}
}
