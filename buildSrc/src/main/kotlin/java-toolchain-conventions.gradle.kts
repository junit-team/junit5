import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

val javaToolchainVersion: String? by project
val minLanguageVersion = JavaLanguageVersion.of(15)!!
val javaLanguageVersion = javaToolchainVersion?.let { JavaLanguageVersion.of(it) } ?: minLanguageVersion
require(javaLanguageVersion >= minLanguageVersion) {
	"Toolchain needs to use at least $minLanguageVersion"
}

project.pluginManager.withPlugin("java") {
	val extension = the<JavaPluginExtension>()
	val javaToolchainService = the<JavaToolchainService>()
	extension.toolchain.languageVersion.set(javaLanguageVersion)
	val compiler = javaToolchainService.compilerFor(extension.toolchain)
	tasks.withType<KotlinJvmCompile>().configureEach {
		doFirst {
			kotlinOptions.jdkHome = compiler.get().metadata.installationPath.asFile.absolutePath
		}
	}
	tasks.withType<JavaCompile>().configureEach {
		javaCompiler.set(compiler)
	}
	tasks.withType<JavaExec>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor(extension.toolchain))
	}
}
