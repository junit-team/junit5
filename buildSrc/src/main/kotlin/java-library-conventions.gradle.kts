import aQute.bnd.gradle.BundleTaskConvention
import aQute.bnd.gradle.FileSetRepositoryConvention
import aQute.bnd.gradle.Resolve

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

val shadowed by configurations.creating
val extension = extensions.create<JavaLibraryExtension>("javaLibrary")

fun javaModuleName(project: Project) = "org." + project.name.replace('-', '.')
val javaModuleName = javaModuleName(project)
val moduleSourceDir = file("src/module/$javaModuleName")
val moduleOutputDir = file("$buildDir/classes/java/module")
val javaVersion = JavaVersion.current()

sourceSets {
	main {
		compileClasspath += shadowed
	}
	test {
		runtimeClasspath += shadowed
	}
	register("mainRelease9") {
		compileClasspath += main.get().output
		runtimeClasspath += main.get().output
		java {
			setSrcDirs(setOf("src/main/java9"))
		}
	}
}

configurations {
	named("mainRelease9CompileClasspath") {
		extendsFrom(compileClasspath.get())
	}
	named("mainRelease9CompileClasspath") {
		extendsFrom(runtimeClasspath.get())
	}
}

eclipse {
	classpath {
		plusConfigurations.add(shadowed)
	}
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

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(shadowed)
	}
}

tasks.javadoc {
	classpath += shadowed
}

tasks.checkstyleMain {
	classpath += shadowed
}

if (project in mavenizedProjects) {

	apply(plugin = "publishing-conventions")

	java {
		withJavadocJar()
		withSourcesJar()
	}

	tasks.javadoc {
		source(sourceSets["mainRelease9"].allJava)
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

	tasks.named<Jar>("sourcesJar") {
		from(sourceSets["mainRelease9"].allSource)
		from(moduleSourceDir) {
			include("module-info.java")
		}
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}

	tasks.withType<Jar>().configureEach {
		from(rootDir) {
			include("LICENSE.md", "LICENSE-notice.md")
			into("META-INF")
		}
		val suffix = archiveClassifier.getOrElse("")
		if (suffix.isBlank() || suffix == "all") { // "all" is used by shadow plugin
			from("$moduleOutputDir/$javaModuleName") {
				include("module-info.class")
			}
		}
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

	// This task enances `jar` and `shadowJar` tasks with the bnd
	// `BundleTaskConvention` convention which allows for generating OSGi
	// metadata into the jar
	tasks.withType<Jar>().matching {
		task: Jar -> task.name == "jar" || task.name == "shadowJar"
	}.configureEach {
		val btc = BundleTaskConvention(this)

		// These are bnd instructions necessary for generating OSGi metadata.
		// We've generalized these so that they are widely applicable limiting
		// module configurations to special cases.
		btc.setBnd("""
			# These are the general rules for package imports.
			Import-Package: \
				!org.apiguardian.api,\
				org.junit.platform.commons.logging;status=INTERNAL,\
				kotlin.*;resolution:="optional",\
				*

			# This tells bnd not to complain if a module doesn't actually import
			# the kotlin packages, but enough modules do to make it a default.
			-fixupmessages.kotlin.import: "Unused Import-Package instructions: \\[kotlin.*\\]";is:=ignore

			# This tells bnd to ignore classes it files in `META-INF/versions/`
			# because bnd doesn't yet support multi-release jars.
			-fixupmessages.wrong.dir: "Classes found in the wrong directory: \\{META-INF/versions/...";is:=ignore

			# Don't scan for Class.forName package imports.
			# See https://bnd.bndtools.org/instructions/noclassforname.html
			-noclassforname: true

			# Don't add all the extra headers bnd normally adds.
			# See https://bnd.bndtools.org/instructions/noextraheaders.html
			-noextraheaders: true

			# Don't add the Private-Package header.
			# See https://bnd.bndtools.org/instructions/removeheaders.html
			-removeheaders: Private-Package

			# Add the custom buildSrc/src/main/kotlin/APIGuardianAnnotations.kt
			# plugin to bnd
			-plugin.apiguardian.annotations: ${APIGuardianAnnotations::class.qualifiedName}

			# Instruct the APIGuardianAnnotations how to operate.
			# See https://bnd.bndtools.org/instructions/export-apiguardian.html
			-export-apiguardian: *;version=${project.version}
		""")

		// Add the convention to the jar task
		convention.plugins.put("bundle", btc)

		doLast {
			// Do the actual work putting OSGi stuff in the jar.
			btc.buildBundle()
		}

		finalizedBy("verifyOSGi")
	}

	// Bnd's Resolve task uses a properties file for it's configuration. This
	// task writes out the properties necessary for it to verify the OSGi
	// metadata.
	tasks.register<WriteProperties>("verifyOSGiProperties") {
		setOutputFile("${buildDir}/verifyOSGiProperties.bndrun")
		property("-standalone", "true")
		property("-runee", "JavaSE-${Versions.jvmTarget}")
		property("-runrequires", "osgi.identity;filter:='(osgi.identity=${project.name})'")
		property("-runsystempackages", "jdk.internal.misc,sun.misc")
	}

	// Bnd's Resolve task is what verifies that a jar can be used in OSGi and
	// that it's metadata is valid. If the metadata is invalid this task will
	// fail.
	tasks.register<Resolve>("verifyOSGi") {
		dependsOn("verifyOSGiProperties")
		setBndrun("${buildDir}/verifyOSGiProperties.bndrun")
		setReportOptional(false)
		withConvention(FileSetRepositoryConvention::class) {

			// By default bnd will use jars found in:
			// 1. project.sourceSets.main.runtimeClasspath
			// 2. project.configurations.archives.artifacts.files
			// to validate the metadata.
			// This adds jars defined in `testRuntimeClasses` also so that bnd
			// can use them to validate the metadata without causing those to
			// end up in the dependencies of those projects.
			bundles(sourceSets["test"].runtimeClasspath)
		}
	}

	// The ${project.description}, for some odd reason, is only available
	// afterEvaluate.
	afterEvaluate {
		tasks.withType<Jar>().configureEach {
			convention.findPlugin(BundleTaskConvention::class.java)
				?.bnd("Bundle-Name: ${project.description}")
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
	dependsOn(tasks.classes, "mainRelease9Classes")
}

tasks.jar {
	dependsOn(allMainClasses)
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

if (modularProjects.contains(project)) {
	val compileModule by tasks.registering(JavaCompile::class) {
		dependsOn(tasks.classes, "mainRelease9Classes")
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
	allMainClasses {
		dependsOn(compileModule)
	}
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

	@get:Input val module: String = javaModuleName(it)

	@get:Input val patch: Provider<FileCollection> = provider {
		if (it == project)
			sourceSets["main"].output + sourceSets["mainRelease9"].output + configurations.compileClasspath.get()
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
		named<JavaCompile>("compileMainRelease9Java").configure {
			sourceCompatibility = "9"
			targetCompatibility = "9"
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
	toolVersion = Versions.checkstyle
	configDirectory.set(rootProject.file("src/checkstyle"))
}

tasks {
	checkstyleMain {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}
	named<Checkstyle>("checkstyleMainRelease9").configure {
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
