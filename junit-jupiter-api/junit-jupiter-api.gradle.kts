import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
	"api"("org.opentest4j:opentest4j:${properties["ota4jVersion"]}")
	"api"(project(":junit-platform-commons"))
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks {
	"jar"(Jar::class) {
		manifest {
			attributes(mapOf(
				"Automatic-Module-Name" to "org.junit.jupiter.api"
			))
		}
	}
}

configurations {
	"apiElements" {
		/*
		 * Needed to configure kotlin to work correctly with the "java-library" plugin.
		 * See:
		 * https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_known_issues
		 * https://youtrack.jetbrains.com/issue/KT-18497
		 */
		val compileKotlin: KotlinCompile by tasks
		outgoing
			.variants
			.getByName("classes")
			.artifact(mapOf(
				"file" to compileKotlin.destinationDir,
				"type" to "java-classes-directory",
				"builtBy" to compileKotlin
			))
	}
}

