plugins {
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.shadow-conventions")
	id("junitbuild.jmh-conventions")
}

description = "JUnit Jupiter Params"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)

	shadowed(libs.univocity.parsers)

	compileOnly(kotlin("stdlib"))

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	jar {
		bundle {
			val version = project.version
			bnd("""
				Require-Capability:\
					org.junit.platform.engine;\
						filter:='(&(org.junit.platform.engine=junit-jupiter)(version>=${'$'}{version_cleanup;$version})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;$version}})))';\
						effective:=active
			""")
		}
	}
}

tasks {
	shadowJar {
		relocate("com.univocity", "org.junit.jupiter.params.shadow.com.univocity")
		from(projectDir) {
			include("LICENSE-univocity-parsers.md")
			into("META-INF")
		}
	}
	compileModule {
		options.compilerArgs.addAll(listOf(
			"--add-modules", "univocity.parsers",
			"--add-reads", "${javaModuleName}=univocity.parsers"
		))
	}
}
