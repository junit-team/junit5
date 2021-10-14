import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`java-library`
	eclipse
	idea
	checkstyle
	id("base-conventions")
	id("jacoco-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra
val modularProjects: List<Project> by rootProject.extra
val buildDate: String by rootProject.extra
val buildTime: String by rootProject.extra
val buildRevision: Any by rootProject.extra
val builtByValue: String by rootProject.extra

val extension = extensions.create<JavaLibraryExtension>("javaLibrary")

val moduleSourceDir = file("src/module/$javaModuleName")
val moduleOutputDir = file("$buildDir/classes/java/module")
val javaVersion = JavaVersion.current()

eclipse {
	jdt {
		file {
			// Set properties for org.eclipse.jdt.core.prefs
			withProperties {
				// Configure Eclipse projects with -parameters compiler flag.
				setProperty("org.eclipse.jdt.core.compiler.codegen.methodParameters", "generate")
			}
		}
	}
}

java {
	modularity.inferModulePath.set(false)
}

if (project in mavenizedProjects) {

	apply(plugin = "publishing-conventions")
	apply(plugin = "osgi-conventions")

	java {
		withJavadocJar()
		withSourcesJar()
	}

	tasks.javadoc {
		options {
			memberLevel = JavadocMemberLevel.PROTECTED
			header = project.name
			encoding = "UTF-8"
			locale = "en"
			(this as StandardJavadocDocletOptions).apply {
				addBooleanOption("Xdoclint:html,syntax", true)
				addBooleanOption("html5", true)
				addMultilineStringsOption("tag").value = listOf(
						"apiNote:a:API Note:",
						"implNote:a:Implementation Note:"
				)
				use(true)
				noTimestamp(true)
			}
		}
	}

	tasks.named<Jar>("javadocJar").configure {
		from(tasks.javadoc.map { File(it.destinationDir, "element-list") }) {
			// For compatibility with older tools, e.g. NetBeans 11
			rename { "package-list" }
		}
	}

	tasks.named<Jar>("sourcesJar").configure {
		from(moduleSourceDir) {
			include("module-info.java")
		}
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}

	pluginManager.withPlugin("java-test-fixtures") {
		val javaComponent = components["java"] as AdhocComponentWithVariants
		javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
		javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
	}

	configure<PublishingExtension> {
		publications {
			named<MavenPublication>("maven") {
				from(components["java"])
				versionMapping {
					allVariants {
						fromResolutionResult()
					}
				}
				pom {
					description.set(provider { "Module \"${project.name}\" of JUnit 5." })
				}
			}
		}
	}

} else {
	tasks {
		jar {
			enabled = false
		}
		javadoc {
			enabled = false
		}
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	dirMode = Integer.parseInt("0755", 8)
	fileMode = Integer.parseInt("0644", 8)
}

normalization {
	runtimeClasspath {
		metaInf {
			// Ignore inconsequential JAR manifest attributes such as timestamps and the commit checksum.
			// This is used when checking whether runtime classpaths, e.g. of test tasks, have changed and
			// improves cacheability of such tasks.
			ignoreAttribute("Built-By")
			ignoreAttribute("Build-Date")
			ignoreAttribute("Build-Time")
			ignoreAttribute("Build-Revision")
			ignoreAttribute("Created-By")
		}
	}
}

val allMainClasses by tasks.registering {
	dependsOn(tasks.classes)
}

val compileModule by tasks.registering(JavaCompile::class) {
	dependsOn(allMainClasses)
	source = fileTree(moduleSourceDir)
	destinationDirectory.set(moduleOutputDir)
	sourceCompatibility = "9"
	targetCompatibility = "9"
	classpath = files()
	options.release.set(9)
	options.compilerArgs.addAll(listOf(
			// Suppress warnings for automatic modules: org.apiguardian.api, org.opentest4j
			"-Xlint:all,-requires-automatic,-requires-transitive-automatic",
			"-Werror", // Terminates compilation when warnings occur.
			"--module-version", "${project.version}",
			"--module-source-path", files(modularProjects.map { "${it.projectDir}/src/module" }).asPath
	))
	options.compilerArgumentProviders.add(ModulePathArgumentProvider())
	options.compilerArgumentProviders.addAll(modularProjects.map { PatchModuleArgumentProvider(it) })
	modularity.inferModulePath.set(false)
}

tasks.withType<Jar>().configureEach {
	from(rootDir) {
		include("LICENSE.md", "LICENSE-notice.md")
		into("META-INF")
	}
	val suffix = archiveClassifier.getOrElse("")
	if (suffix.isBlank() || this is ShadowJar) {
		dependsOn(allMainClasses, compileModule)
		from("$moduleOutputDir/$javaModuleName") {
			include("module-info.class")
		}
	}
}

tasks.jar {
	manifest {
		attributes(
				"Created-By" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})",
				"Built-By" to builtByValue,
				"Build-Date" to buildDate,
				"Build-Time" to buildTime,
				"Build-Revision" to buildRevision,
				"Specification-Title" to project.name,
				"Specification-Version" to (project.version as String).substringBefore('-'),
				"Specification-Vendor" to "junit.org",
				"Implementation-Title" to project.name,
				"Implementation-Version" to project.version,
				"Implementation-Vendor" to "junit.org"
		)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.compileJava {
	// See: https://docs.oracle.com/en/java/javase/12/tools/javac.html
	options.compilerArgs.addAll(listOf(
			"-Xlint:all", // Enables all recommended warnings.
			"-Werror" // Terminates compilation when warnings occur.
	))
}

tasks.compileTestJava {
	// See: https://docs.oracle.com/en/java/javase/12/tools/javac.html
	options.compilerArgs.addAll(listOf(
			"-Xlint", // Enables all recommended warnings.
			"-Xlint:-overrides", // Disables "method overrides" warnings.
			"-Werror", // Terminates compilation when warnings occur.
			"-parameters" // Generates metadata for reflection on method parameters.
	))
}

inner class ModulePathArgumentProvider : CommandLineArgumentProvider, Named {
	@get:CompileClasspath
	val modulePath: Provider<Configuration> = configurations.compileClasspath
	override fun asArguments() = listOf("--module-path", modulePath.get().asPath)
	@Internal
	override fun getName() = "module-path"
}

inner class PatchModuleArgumentProvider(it: Project) : CommandLineArgumentProvider, Named {

	@get:Input
	val module: String = it.javaModuleName

	@get:InputFiles
	@get:PathSensitive(RELATIVE)
	val patch: Provider<FileCollection> = provider {
		if (it == project)
			files(sourceSets.matching { it.name.startsWith("main") }.map { it.output })
		else
			files(it.sourceSets["main"].java.srcDirs)
	}

	override fun asArguments(): List<String> {
		val path = patch.get().filter { it.exists() }.asPath
		if (path.isEmpty()) {
			return emptyList()
		}
		return listOf("--patch-module", "$module=$path")
	}

	@Internal
	override fun getName() = "patch-module($module)"
}

afterEvaluate {
	configurations {
		apiElements {
			attributes {
				attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.majorVersion.toInt())
			}
		}
		runtimeElements {
			attributes {
				attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.majorVersion.toInt())
			}
		}
	}
	tasks {
		compileJava {
			if (extension.configureRelease) {
				options.release.set(extension.mainJavaVersion.majorVersion.toInt())
			} else {
				sourceCompatibility = extension.mainJavaVersion.majorVersion
				targetCompatibility = extension.mainJavaVersion.majorVersion
			}
		}
		compileTestJava {
			if (extension.configureRelease) {
				options.release.set(extension.testJavaVersion.majorVersion.toInt())
			} else {
				sourceCompatibility = extension.testJavaVersion.majorVersion
				targetCompatibility = extension.testJavaVersion.majorVersion
			}
		}
	}
	pluginManager.withPlugin("groovy") {
		tasks.named<GroovyCompile>("compileGroovy").configure {
			if (extension.configureRelease) {
				options.release.set(extension.mainJavaVersion.majorVersion.toInt())
			} else {
				sourceCompatibility = extension.mainJavaVersion.majorVersion
				targetCompatibility = extension.mainJavaVersion.majorVersion
			}
		}
		tasks.named<GroovyCompile>("compileTestGroovy").configure {
			if (extension.configureRelease) {
				options.release.set(extension.testJavaVersion.majorVersion.toInt())
			} else {
				sourceCompatibility = extension.testJavaVersion.majorVersion
				targetCompatibility = extension.testJavaVersion.majorVersion
			}
		}
	}
}

checkstyle {
	toolVersion = requiredVersionFromLibs("checkstyle")
	configDirectory.set(rootProject.file("src/checkstyle"))
}

tasks {
	checkstyleMain {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}
	checkstyleTest {
		configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
	}
}

pluginManager.withPlugin("java-test-fixtures") {
	tasks.named<Checkstyle>("checkstyleTestFixtures") {
		configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
	}
	tasks.named<JavaCompile>("compileTestFixturesJava") {
		options.release.set(extension.testJavaVersion.majorVersion.toInt())
	}
}
