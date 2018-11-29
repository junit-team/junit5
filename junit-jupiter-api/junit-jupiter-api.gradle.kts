import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.ProjectDependency

description = "JUnit Jupiter API"

dependencies {
	api("org.opentest4j:opentest4j:${Versions.ota4j}")
	api(project(":junit-platform-commons"))
	compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	runtimeOnly(project(":junit-jupiter-engine"))
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.api"
		)
	}
}

// Remove runtimeOnly dependency on junit-jupiter-engine and junit-platform-engine.
// See https://github.com/junit-team/junit5/issues/1669
eclipse.classpath.file.whenMerged(Action<Classpath> {
	entries.removeAll {
		it is ProjectDependency &&
				(it.path.contains("junit-jupiter-engine") || it.path.contains("junit-platform-engine"))
	}
})
