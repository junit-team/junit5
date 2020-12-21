import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

val javaToolchainVersion: String? by project
val defaultLanguageVersion = JavaLanguageVersion.of(15)
val javaLanguageVersion = javaToolchainVersion?.let { JavaLanguageVersion.of(it) } ?: defaultLanguageVersion

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
		// Temporary workaround for https://github.com/gradle/gradle/issues/15538
		options.forkOptions.jvmArgs!!.addAll(listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))
	}
	tasks.withType<GroovyCompile>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor {
			// Groovy does not yet support JDK 16, see https://issues.apache.org/jira/browse/GROOVY-9752
			languageVersion.set(minOf(javaLanguageVersion, defaultLanguageVersion))
		})
	}
	tasks.withType<JavaExec>().configureEach {
		javaLauncher.set(javaToolchainService.launcherFor(extension.toolchain))
	}
}
