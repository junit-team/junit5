import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
	id("junitbuild.build-parameters")
}

project.pluginManager.withPlugin("java") {
	val defaultLanguageVersion = JavaLanguageVersion.of(21)
	val javaLanguageVersion = buildParameters.javaToolchainVersion.map { JavaLanguageVersion.of(it) }.getOrElse(defaultLanguageVersion)

	val extension = the<JavaPluginExtension>()
	val javaToolchainService = the<JavaToolchainService>()

	extension.toolchain.languageVersion = javaLanguageVersion

	pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
		configure<KotlinJvmProjectExtension> {
			jvmToolchain {
				languageVersion = javaLanguageVersion
			}
		}
	}

	tasks.withType<JavaExec>().configureEach {
		javaLauncher = javaToolchainService.launcherFor(extension.toolchain)
	}

	tasks.withType<JavaCompile>().configureEach {
		outputs.cacheIf { javaLanguageVersion == defaultLanguageVersion }
		doFirst {
			if (options.release.orNull == 8 && javaLanguageVersion.asInt() >= 20) {
				options.compilerArgs.add(
					"-Xlint:-options" // see https://github.com/junit-team/junit5/issues/3029
				)
			}
		}
	}

	tasks.withType<GroovyCompile>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor {
			// Groovy does not yet support JDK 19, see https://issues.apache.org/jira/browse/GROOVY-10569
			languageVersion = defaultLanguageVersion
		})
	}
}
