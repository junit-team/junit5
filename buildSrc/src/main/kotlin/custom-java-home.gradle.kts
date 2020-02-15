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
			doFirst {
				// Avoid compiler warnings for non-existing path entries
				classpath = classpath.filter { it.exists() }
			}
		}
		withType<GroovyCompile>().configureEach {
			options.isFork = true
			options.forkOptions.javaHome = file(javaHome)
		}
		withType<KotlinJvmCompile>().configureEach {
			kotlinOptions {
				jdkHome = javaHome
			}
		}
		withType<Javadoc>().configureEach {
			executable = javaHomeExecutable("javadoc")
		}
		withType<Test>().configureEach {
			executable = javaHomeExecutable("java")
		}
		withType<JavaExec>().configureEach {
			setExecutable(javaHomeExecutable("java"))
		}
	}
}
