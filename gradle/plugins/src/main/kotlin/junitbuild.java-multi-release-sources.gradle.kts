plugins {
	id("junitbuild.java-library-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra

listOf(9, 17, 19).forEach { javaVersion ->
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
			options.release.set(javaVersion)
			if (javaVersion == 19) options.compilerArgs.add("--enable-preview")
		}

		named<Checkstyle>("checkstyle${sourceSet.name.replaceFirstChar(Char::titlecase)}").configure {
			configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
		}

		if (project in mavenizedProjects) {
			javadoc {
				source(sourceSet.get().allJava)
                val javadocOptions = options as CoreJavadocOptions
                if (javaVersion == 19) {
                    javadocOptions.addStringOption("-release", "19")
                    javadocOptions.addBooleanOption("-enable-preview", true)
                }
			}
			named<Jar>("sourcesJar").configure {
				from(sourceSet.get().allSource)
			}
		}
	}
}
