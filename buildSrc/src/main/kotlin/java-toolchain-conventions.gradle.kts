import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

val javaToolchainVersion: String? by project

project.pluginManager.withPlugin("java") {
	val extension = the<JavaPluginExtension>()
	val javaToolchainService = the<JavaToolchainService>()
	extension.toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion?.toInt() ?: 11))
	}
	tasks.withType<KotlinJvmCompile>().configureEach {
		doFirst {
			kotlinOptions.jdkHome = javaToolchainService.compilerFor(extension.toolchain).get().metadata.installationPath.asFile.absolutePath
		}
	}
}
