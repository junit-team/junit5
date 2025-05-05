plugins {
	id("junitbuild.kotlin-library-conventions")
}

description = "JUnit Jupiter Kotlin support"

dependencies {
	implementation(projects.junitJupiterEngine)
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	implementation(libs.kotlinx.coroutines)
}

tasks.verifyOSGi {
	// TODO Fix the following error:
	// Resolution failed. Summary:
	//      ⇒ Bundle: junit-jupiter-kotlin cannot be resolved
	//              ⇒ because Import-Package requirement for: kotlinx.coroutines could not be provided by any available bundle or dependency
	//
	//Note: The summary above may be incomplete. Please check the full output below for more hints.
	//Resolution failed. Capabilities satisfying the following requirements could not be found:
	//
	//    [<<INITIAL>>]
	//      ⇒ osgi.identity: (osgi.identity=junit-jupiter-kotlin)
	//          ⇒ [junit-jupiter-kotlin version=6.0.0.SNAPSHOT]
	//              ⇒ osgi.wiring.package: (osgi.wiring.package=kotlinx.coroutines)
	enabled = false
}
