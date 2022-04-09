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

val mainRelease17 by sourceSets.registering {
	compileClasspath += sourceSets.main.get().output
	runtimeClasspath += sourceSets.main.get().output
	java {
		setSrcDirs(setOf("src/main/java17"))
	}
}

configurations.named(mainRelease9.get().compileClasspathConfigurationName).configure {
	extendsFrom(configurations.compileClasspath.get())
}

configurations.named(mainRelease17.get().compileClasspathConfigurationName).configure {
	extendsFrom(configurations.compileClasspath.get())
}

tasks {

	named("allMainClasses").configure {
		dependsOn(mainRelease9.get().classesTaskName)
		dependsOn(mainRelease17.get().classesTaskName)
	}

	named<JavaCompile>(mainRelease9.get().compileJavaTaskName).configure {
		options.release.set(9)
	}
	named<JavaCompile>(mainRelease17.get().compileJavaTaskName).configure {
		options.release.set(17)
	}

	named<Checkstyle>("checkstyle${mainRelease9.name.capitalize()}").configure {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}
	named<Checkstyle>("checkstyle${mainRelease17.name.capitalize()}").configure {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}

	if (project in mavenizedProjects) {
		javadoc {
			source(mainRelease9.get().allJava)
			source(mainRelease17.get().allJava)
		}
		named<Jar>("sourcesJar").configure {
			from(mainRelease9.get().allSource)
			from(mainRelease17.get().allSource)
		}
	}
}
