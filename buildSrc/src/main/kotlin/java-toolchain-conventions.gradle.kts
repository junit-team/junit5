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
}
