plugins {
	id("junitbuild.kotlin-library-conventions")
}

description = "JUnit Jupiter Kotlin support"

dependencies {
	implementation(projects.junitJupiterEngine)
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	implementation(libs.kotlinx.coroutines)
	osgiVerification(kotlin("osgi-bundle"))
}

tasks.jar {
	bundle {
		val importAPIGuardian: String by extra
		// Mark kotlinx.coroutines as optional since there's no OSGi bundle for it
		bnd("""
				Import-Package: \
					$importAPIGuardian,\
					kotlinx.coroutines.*;resolution:="optional",\
					*
			""")
	}
}
