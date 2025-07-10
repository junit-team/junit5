import junitbuild.extensions.dependencyFromLibs
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
	`java-library`
	id("net.ltgt.errorprone")
	id("net.ltgt.nullaway")
}

dependencies {
	errorprone(dependencyFromLibs("errorProne-core"))
	errorprone(dependencyFromLibs("nullaway"))
}

nullaway {
	onlyNullMarked = true
}

tasks.withType<JavaCompile>().configureEach {
	options.errorprone {
		val onJ9 = java.toolchain.implementation.orNull == JvmImplementation.J9
		if (name == "compileJava" && !onJ9) {
			disable(
				"BadImport",
				"UnnecessaryLambda",
				"AnnotateFormatMethod",
				"StringSplitter",
				"DoNotCallSuggester",
				"InlineMeSuggester",
				"ImmutableEnumChecker",
				"MissingSummary"
			)
			error("PackageLocation")
		} else {
			disableAllChecks = true
		}
		nullaway {
			if (onJ9) {
				disable()
			} else {
				enable()
			}
			isJSpecifyMode = true
		}
	}
}

tasks.withType<JavaCompile>().named { it.startsWith("compileTest") }.configureEach {
	options.errorprone.nullaway {
		handleTestAssertionLibraries = true
		excludedFieldAnnotations.addAll(
			"org.junit.jupiter.api.io.TempDir",
			"org.junit.jupiter.params.Parameter",
			"org.junit.runners.Parameterized.Parameter",
			"org.mockito.Captor",
			"org.mockito.InjectMocks",
			"org.mockito.Mock",
			"org.mockito.Spy",
		)
	}
}
