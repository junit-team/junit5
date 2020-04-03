plugins {
	`java-platform`
}

val String.version: String get() = rootProject.extra["$this.version"] as String

fun DependencyConstraintHandlerScope.apiv(
		notation: String,
		versionProp: String = notation.substringAfterLast(':')
) =
		"api"(notation + ":" + versionProp.version)

fun DependencyConstraintHandlerScope.runtimev(
		notation: String,
		versionProp: String = notation.substringAfterLast(':')
) =
		"runtime"(notation + ":" + versionProp.version)

dependencies {
	constraints {
		// api means "the dependency is for both compilation and runtime"
		// runtime means "the dependency is only for runtime, not for compilation"
		// In other words, marking dependency as "runtime" would avoid accidental
		// dependency on it during compilation
		apiv("org.apiguardian:apiguardian-api")
		apiv("org.opentest4j:opentest4j")
		runtimev("org.apache.logging.log4j:log4j-core", "log4j")
		runtimev("org.apache.logging.log4j:log4j-jul", "log4j")
		apiv("io.github.classgraph:classgraph")
		apiv("org.codehaus.groovy:groovy-all", "groovy")
		api("junit:junit:[${"junit4Min".version},)") {
			version {
				prefer("junit4".version)
			}
		}
		apiv("com.univocity:univocity-parsers")
		apiv("info.picocli:picocli")
		apiv("org.assertj:assertj-core", "assertj")
		apiv("org.openjdk.jmh:jmh-core", "jmh")
		apiv("org.openjdk.jmh:jmh-generator-annprocess", "jmh")
		apiv("de.sormuras:bartholdy")
		apiv("commons-io:commons-io")
		apiv("com.tngtech.archunit:archunit-junit5-api", "archunit")
		apiv("com.tngtech.archunit:archunit-junit5-engine", "archunit")
		apiv("org.slf4j:slf4j-jdk14", "slf4j")
		apiv("org.jetbrains.kotlinx:kotlinx-coroutines-core")
		apiv("org.mockito:mockito-junit-jupiter", "mockito")
		apiv("biz.aQute.bnd:biz.aQute.bndlib", "bnd")
	}
}
