val javaToolchainVersion: String? by project
val defaultLanguageVersion = JavaLanguageVersion.of(17)
val javaLanguageVersion = javaToolchainVersion?.let { JavaLanguageVersion.of(it) } ?: defaultLanguageVersion

project.pluginManager.withPlugin("java") {
	val extension = the<JavaPluginExtension>()
	val javaToolchainService = the<JavaToolchainService>()
	extension.toolchain.languageVersion.set(javaLanguageVersion)
	tasks.withType<JavaExec>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor(extension.toolchain))
	}
	tasks.withType<JavaCompile>().configureEach {
		outputs.cacheIf { javaLanguageVersion == defaultLanguageVersion }
	}
	tasks.withType<GroovyCompile>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor {
			// Groovy does not yet support JDK 19, see https://issues.apache.org/jira/browse/GROOVY-10569
			languageVersion.set(defaultLanguageVersion)
		})
	}
	if (javaLanguageVersion.asInt() >= 20) {
		tasks.compileJava {
			options.compilerArgs.add(
				"-Xlint:-options" // see https://github.com/junit-team/junit5/issues/3029
			)
		}
	}
}
