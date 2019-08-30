import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

if (project.hasProperty("javaHome")) {

	val javaHome: String by project
	require(file(javaHome).isDirectory) {
		"Java home directory set via `javaHome` project property is invalid: $javaHome"
	}

	fun javaHomeExecutable(execName: String): String {
		val extension = if (OperatingSystem.current().isWindows) ".exe" else ""
		val executable = File(File(javaHome, "bin"), "$execName$extension")
		require(executable.exists()) {
			"File does not exist: $executable"
		}
		return executable.canonicalPath
	}

	tasks {
		withType<JavaCompile>().configureEach {
			options.isFork = true
			options.forkOptions.javaHome = file(javaHome)
			inputs.property("javaHome", javaHome)
			doFirst {
				// Avoid compiler warnings for non-existing path entries
				classpath = classpath.filter { it.exists() }
			}
		}
		withType<GroovyCompile>().configureEach {
			options.isFork = true
			options.forkOptions.javaHome = file(javaHome)
			inputs.property("javaHome", javaHome)
		}
		withType<KotlinJvmCompile>().configureEach {
			kotlinOptions {
				jdkHome = javaHome
			}
			inputs.property("javaHome", javaHome)
		}
		withType<Javadoc>().configureEach {
			executable = javaHomeExecutable("javadoc")
			inputs.property("javaHome", javaHome)
		}
		withType<Test>().configureEach {
			executable = javaHomeExecutable("java")
			inputs.property("javaHome", javaHome)
		}
		withType<JavaExec>().configureEach {
			setExecutable(javaHomeExecutable("java"))
			inputs.property("javaHome", javaHome)
		}
	}

}
