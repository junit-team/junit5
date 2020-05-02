plugins {
	`java-library`
	eclipse
	idea
	checkstyle
	id("custom-java-home")
}

val mavenizedProjects: List<Project> by rootProject.extra
val modularProjects: List<Project> by rootProject.extra
val buildDate: String by rootProject.extra
val buildTime: String by rootProject.extra
val buildRevision: Any by rootProject.extra
val builtByValue: String by rootProject.extra

val internal by configurations.creating {
	isVisible = false
	isCanBeConsumed = false
	isCanBeResolved = false
}

val extension = extensions.create<JavaLibraryExtension>("javaLibrary")

val moduleSourceDir = file("src/module/$javaModuleName")
val moduleOutputDir = file("$buildDir/classes/java/module")
val javaVersion = JavaVersion.current()

configurations {
	compileClasspath.get().extendsFrom(internal)
	runtimeClasspath.get().extendsFrom(internal)
	testCompileClasspath.get().extendsFrom(internal)
	testRuntimeClasspath.get().extendsFrom(internal)
}

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
				// Javadoc 13 removed support for `--no-module-directories`
				// https://bugs.openjdk.java.net/browse/JDK-8215580
				if (javaVersion.isJava12 && executable == null) {
					addBooleanOption("-no-module-directories", true)
				}
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
		configurations["testFixturesCompileClasspath"].extendsFrom(internal)
		configurations["testFixturesRuntimeClasspath"].extendsFrom(internal)
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

normalization {
	runtimeClasspath {
		// Ignore the JAR manifest when checking whether runtime classpath have changed
		// because it contains timestamps and the commit checksum. This is used when
		// checking whether a test task is up-to-date or can be loaded from the build cache.
		ignore("/META-INF/MANIFEST.MF")
	}
}

val allMainClasses by tasks.registering {
	dependsOn(tasks.classes)
}

val compileModule by tasks.registering(JavaCompile::class) {
	dependsOn(allMainClasses)
	source = fileTree(moduleSourceDir)
	destinationDir = moduleOutputDir
	sourceCompatibility = "9"
	targetCompatibility = "9"
	classpath = files()
	options.compilerArgs.addAll(listOf(
			// "-verbose",
			// Suppress warnings for automatic modules: org.apiguardian.api, org.opentest4j
			"-Xlint:all,-requires-automatic,-requires-transitive-automatic",
			"--release", "9",
			"--module-version", "${project.version}",
			"--module-source-path", files(modularProjects.map { "${it.projectDir}/src/module" }).asPath
	))
	options.compilerArgumentProviders.add(ModulePathArgumentProvider())
	options.compilerArgumentProviders.addAll(modularProjects.map { PatchModuleArgumentProvider(it) })
}

tasks.withType<Jar>().configureEach {
	from(rootDir) {
		include("LICENSE.md", "LICENSE-notice.md")
		into("META-INF")
	}
	val suffix = archiveClassifier.getOrElse("")
	if (suffix.isBlank() || suffix == "all") { // "all" is used by shadow plugin
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

inner class ModulePathArgumentProvider : CommandLineArgumentProvider {
	@get:Input val modulePath: Provider<Configuration> = configurations.compileClasspath
	override fun asArguments(): List<String> = listOf("--module-path", modulePath.get().asPath)
}

inner class PatchModuleArgumentProvider(it: Project) : CommandLineArgumentProvider {

	@get:Input val module: String = it.javaModuleName

	@get:Input val patch: Provider<FileCollection> = provider {
		if (it == project)
			files(sourceSets.matching { it.name.startsWith("main") }.map { it.output }) + configurations.compileClasspath.get()
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
			sourceCompatibility = extension.mainJavaVersion.majorVersion
			targetCompatibility = extension.mainJavaVersion.majorVersion
		}
		compileTestJava {
			sourceCompatibility = extension.testJavaVersion.majorVersion
			targetCompatibility = extension.testJavaVersion.majorVersion
		}
		withType<JavaCompile>().configureEach {
			// --release release
			// Compiles against the public, supported and documented API for a specific VM version.
			// Supported release targets are 7, 8, 9, 10, 11, 12
			// Note that if --release is added then -target and -source are ignored.
			options.compilerArgs.addAll(listOf("--release", targetCompatibility))
		}
	}
	pluginManager.withPlugin("groovy") {
		tasks.named<GroovyCompile>("compileGroovy").configure {
			sourceCompatibility = extension.mainJavaVersion.majorVersion
			targetCompatibility = extension.mainJavaVersion.majorVersion
		}
		tasks.named<GroovyCompile>("compileTestGroovy").configure {
			sourceCompatibility = extension.testJavaVersion.majorVersion
			targetCompatibility = extension.testJavaVersion.majorVersion
		}
	}
}

checkstyle {
	toolVersion = versions["checkstyle"]
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
	tasks.named<Checkstyle>("checkstyleTestFixtures").configure {
		configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
	}
}
