plugins {
	id("java-library-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra

val extension = extensions.create<MultiReleaseSourcesExtension>("multiReleaseSources")

extension.releases.all {
	val release = this

	val releaseSourceSet = sourceSets.create("mainRelease$release") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
		java {
			setSrcDirs(setOf("src/main/java$release"))
		}
	}

	configurations.named(releaseSourceSet.compileClasspathConfigurationName).configure {
		extendsFrom(configurations.compileClasspath.get())
	}

	tasks {

		named("allMainClasses").configure {
			dependsOn(releaseSourceSet.classesTaskName)
		}

		named<JavaCompile>(releaseSourceSet.compileJavaTaskName).configure {
			options.release.set(release)
		}

		named<Checkstyle>("checkstyle${releaseSourceSet.name.capitalize()}").configure {
			configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
		}

		if (project in mavenizedProjects) {
			javadoc {
				source(releaseSourceSet.allJava)
			}
			named<Jar>("sourcesJar").configure {
				from(releaseSourceSet.allSource)
			}
		}
	}
}
