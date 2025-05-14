plugins {
	id("junitbuild.kotlin-library-conventions")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)

	compileOnly(kotlin("stdlib"))
	compileOnly(kotlin("reflect"))
	compileOnly(libs.kotlinx.coroutines)
}

tasks.compileJava {
	options.compilerArgs.add("-Xlint:-module") // due to qualified exports
	options.compilerArgumentProviders.add(CommandLineArgumentProvider {
		listOf("--patch-module", "${javaModuleName}=${sourceSets.main.get().output.asPath}")
	})
}

tasks.jar {
	bundle {
		val importAPIGuardian: String by extra
		bnd("""
			Import-Package: \
				$importAPIGuardian,\
				kotlin.*;resolution:="optional",\
				kotlinx.*;resolution:="optional",\
				*
		""")
	}
}
