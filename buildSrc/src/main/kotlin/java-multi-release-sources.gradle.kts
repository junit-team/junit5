plugins {
	id("java-library-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra

val mainRelease9 by sourceSets.registering {
	compileClasspath += sourceSets.main.get().output
	runtimeClasspath += sourceSets.main.get().output
	java {
		setSrcDirs(setOf("src/main/java9"))
	}
}

configurations.named(mainRelease9.get().compileClasspathConfigurationName).configure {
	extendsFrom(configurations.compileClasspath.get())
}

tasks {

	named("allMainClasses").configure {
		dependsOn(mainRelease9.get().classesTaskName)
	}

	named<JavaCompile>(mainRelease9.get().compileJavaTaskName).configure {
		sourceCompatibility = "9"
		targetCompatibility = "9"
	}

	named<Checkstyle>("checkstyle${mainRelease9.name.capitalize()}").configure {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}

	if (project in mavenizedProjects) {
		javadoc {
			source(mainRelease9.get().allJava)
		}
		named<Jar>("sourcesJar").configure {
			from(mainRelease9.get().allSource)
		}
	}
}
