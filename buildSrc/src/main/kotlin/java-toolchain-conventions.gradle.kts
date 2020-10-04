import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

val javaToolchainVersion: String? by project

project.pluginManager.withPlugin("java") {
	val extension = the<JavaPluginExtension>()
	val javaLanguageVersion = JavaLanguageVersion.of(javaToolchainVersion?.toInt() ?: 11)
	extension.toolchain {
		languageVersion.set(javaLanguageVersion)
	}
	val service = the<JavaToolchainService>()
	val javaHome = service.compilerFor(extension.toolchain).get().metadata.installationPath.asFile.absolutePath
	tasks.withType<KotlinJvmCompile>().configureEach {
		kotlinOptions {
			jdkHome = javaHome
		}
	}
}
