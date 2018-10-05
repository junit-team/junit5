import org.asciidoctor.gradle.AsciidoctorTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.ByteArrayOutputStream
import java.nio.file.Files

buildscript {
	dependencies {
		// upgrade to latest jruby version due to a bugfix needed for Windows 10.
		// can be removed, when asciidoctorj uses this as a default version.
		classpath("org.jruby:jruby-complete:9.1.17.0")

		// classpath("org.asciidoctor:asciidoctorj-epub3:1.5.0-alpha.16")
		classpath("org.asciidoctor:asciidoctorj-pdf:1.5.0-alpha.16")
		classpath("org.asciidoctor:asciidoctorj-diagram:1.5.9")
	}
}

plugins {
	id("org.asciidoctor.convert") version "1.5.8.1"
}

val consoleLauncherTest by tasks.creating(JavaExec::class) {
	dependsOn("testClasses")
	val reportsDir = file("$buildDir/test-results")
	outputs.dir(reportsDir)
	classpath(sourceSets["test"].runtimeClasspath)
	main = "org.junit.platform.console.ConsoleLauncher"
	args("--scan-classpath")
	args("--details", "tree")
	args("--include-classname", ".*Tests")
	args("--include-classname", ".*Demo")
	args("--exclude-tag", "exclude")
	args("--reports-dir", reportsDir)
	systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
}
tasks.named<Test>("test") {
	dependsOn(consoleLauncherTest)
	exclude("**/*")
}

dependencies {
	"asciidoctor"("org.jruby:jruby-complete:9.1.17.0")

	// Jupiter API is used in src/main/java
	implementation(project(":junit-jupiter-api"))

	testImplementation(project(":junit-jupiter-params"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")

	// Required by :consoleLauncherTest and :generateConsoleLauncherOptions
	testRuntimeOnly(project(":junit-platform-console"))
	testRuntimeOnly(project(":junit-platform-suite-api"))

	testRuntimeOnly(project(":junit-vintage-engine"))
	testRuntimeOnly(project(":junit-jupiter-engine"))
	val log4jVersion: String by project
	testRuntimeOnly("org.apache.logging.log4j:log4j-core:${log4jVersion}")
	testRuntimeOnly("org.apache.logging.log4j:log4j-jul:${log4jVersion}")

	// for ApiReportGenerator
	testImplementation("io.github.classgraph:classgraph:4.2.2")
}

asciidoctorj {
	version = "1.5.7"
}

val generatedAsciiDocPath = file("$buildDir/generated/asciidoc")
val consoleLauncherOptionsFile = File(generatedAsciiDocPath, "console-launcher-options.txt")
val experimentalApisTableFile = File(generatedAsciiDocPath, "experimental-apis-table.txt")
val deprecatedApisTableFile = File(generatedAsciiDocPath, "deprecated-apis-table.txt")

fun createJavaExecTaskWithOutputFile(taskName: String, outputFile: File, mainClass: String, mainArgs: List<String> = listOf()) {
	tasks.create<JavaExec>(taskName) {
		outputs.file(outputFile)
		classpath = sourceSets["test"].runtimeClasspath
		main = mainClass
		args = mainArgs
		val byteStream = ByteArrayOutputStream()
		standardOutput = byteStream
		doLast {
			Files.createDirectories(outputFile.parentFile.toPath())
			Files.write(outputFile.toPath(), byteStream.toByteArray())
		}
	}
}

createJavaExecTaskWithOutputFile("generateConsoleLauncherOptions", consoleLauncherOptionsFile, "org.junit.platform.console.ConsoleLauncher", listOf("--help"))

createJavaExecTaskWithOutputFile("generateExperimentalApisTable", experimentalApisTableFile, "org.junit.api.tools.ApiReportGenerator", listOf("EXPERIMENTAL"))
createJavaExecTaskWithOutputFile("generateDeprecatedApisTable", deprecatedApisTableFile, "org.junit.api.tools.ApiReportGenerator", listOf("DEPRECATED"))

// does currently not work on Java 11, see https://github.com/junit-team/junit5/issues/1608
val enableAsciidoctorPdfBackend = JavaVersion.current() < JavaVersion.VERSION_11

tasks.named<AsciidoctorTask>("asciidoctor") {
	dependsOn("generateConsoleLauncherOptions", "generateExperimentalApisTable", "generateDeprecatedApisTable")

	// enable the Asciidoctor Diagram extension
	requires("asciidoctor-diagram")

	separateOutputDirs = false
	sources(delegateClosureOf<PatternSet> {
		include("**/index.adoc")
	})
	resources(delegateClosureOf<CopySpec> {
		from(sourceDir) {
			include("**/images/**")
		}
	})

	backends("html5")
	if (enableAsciidoctorPdfBackend) {
		backends("pdf")
		attributes(mapOf("linkToPdf" to enableAsciidoctorPdfBackend.toString()))
	}

	attributes(mapOf(
			"jupiter-version" to version,
			"platform-version" to project.properties["platformVersion"],
			"vintage-version" to project.properties["vintageVersion"],
			"bom-version" to version,
			"junit4-version" to project.properties["junit4Version"],
			"apiguardian-version" to project.properties["apiGuardianVersion"],
			"ota4j-version" to project.properties["ota4jVersion"],
			"surefire-version" to project.properties["surefireVersion"],
			"release-branch" to project.properties["releaseBranch"],
			"docs-version" to project.properties["docsVersion"],
			"revnumber" to version,
			"consoleLauncherOptionsFile" to consoleLauncherOptionsFile,
			"experimentalApisTableFile" to experimentalApisTableFile,
			"deprecatedApisTableFile" to deprecatedApisTableFile,
			"outdir" to outputDir.absolutePath,
			"source-highlighter" to "coderay@", // TODO switch to "rouge" once supported by the html5 backend and on MS Windows
			"tabsize" to "4",
			"toc" to "left",
			"icons" to "font",
			"sectanchors" to true,
			"idprefix" to "",
			"idseparator" to "-"
	))

	sourceSets["test"].apply {
		attributes(mapOf(
				"testDir" to java.srcDirs.first(),
				"testResourcesDir" to resources.srcDirs.first()
		))
		withConvention(KotlinSourceSet::class) {
			attributes(mapOf("kotlinTestDir" to kotlin.srcDirs.first()))
		}
	}
}

eclipse {
	classpath {
		plusConfigurations.add(project(":junit-platform-console").configurations["shadowed"])
		plusConfigurations.add(project(":junit-jupiter-params").configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(project(":junit-platform-console").configurations["shadowed"])
		scopes["PROVIDED"]!!["plus"]!!.add(project(":junit-jupiter-params").configurations["shadowed"])
	}
}
