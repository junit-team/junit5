plugins {
	`java-library-conventions`
	id("com.github.johnrengelman.shadow")
}

description = "JUnit Platform Console"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-reporting"))

	shadowed("info.picocli:picocli:${Versions.picocli}")
}

tasks {
	shadowJar {
		classifier = ""
		configurations = listOf(project.configurations["shadowed"])
		exclude("META-INF/maven/**")
		relocate("picocli", "org.junit.platform.console.shadow.picocli")
		from(projectDir) {
			include("LICENSE-picocli.md")
			into("META-INF")
		}
	}
	jar {
		enabled = false
		dependsOn(shadowJar)
		manifest {
			attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
		}
	}
}
