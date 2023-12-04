import org.gradle.configurationcache.extensions.capitalized

plugins {
	id("junitbuild.java-library-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra

listOf(9, 17).forEach { javaVersion ->
	val sourceSet = sourceSets.register("mainRelease${javaVersion}") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output
		java {
			setSrcDirs(setOf("src/main/java${javaVersion}"))
		}
	}

	configurations.named(sourceSet.get().compileClasspathConfigurationName).configure {
		extendsFrom(configurations.compileClasspath.get())
	}

	tasks {

		named("allMainClasses").configure {
			dependsOn(sourceSet.get().classesTaskName)
		}

		named<JavaCompile>(sourceSet.get().compileJavaTaskName).configure {
			options.release = javaVersion
		}

		named<Checkstyle>("checkstyle${sourceSet.name.capitalized()}").configure {
            config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleMain.xml"))
		}

		if (project in mavenizedProjects) {
			javadoc {
				source(sourceSet.get().allJava)
			}
			named<Jar>("sourcesJar").configure {
				from(sourceSet.get().allSource)
			}
		}
	}
}
