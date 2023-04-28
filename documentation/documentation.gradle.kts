import junitbuild.exec.ClasspathSystemPropertyProvider
import junitbuild.exec.RunConsoleLauncher
import junitbuild.javadoc.ModuleSpecificJavadocFileOption
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import java.io.ByteArrayOutputStream
import java.nio.file.Files

plugins {
	alias(libs.plugins.asciidoctorConvert)
	alias(libs.plugins.asciidoctorPdf)
	alias(libs.plugins.gitPublish)
	id("junitbuild.build-parameters")
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.testing-conventions")
}

val modularProjects: List<Project> by rootProject

// Because we need to set up Javadoc aggregation
modularProjects.forEach { evaluationDependsOn(it.path) }

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_1_8
	testJavaVersion = JavaVersion.VERSION_1_8
}

val apiReport by configurations.creatingResolvable
val standaloneConsoleLauncher by configurations.creatingResolvable

dependencies {
	implementation(projects.junitJupiterApi) {
		because("Jupiter API is used in src/main/java")
	}

	// Pull in all "modular projects" to ensure that they are included
	// in reports generated by the ApiReportGenerator.
	modularProjects.forEach { apiReport(it) }

	testImplementation(projects.junitJupiterMigrationsupport)
	testImplementation(projects.junitPlatformConsole)
	testImplementation(projects.junitPlatformRunner)
	testImplementation(projects.junitPlatformSuite)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(kotlin("stdlib"))

	testImplementation(projects.junitVintageEngine)
	testRuntimeOnly(libs.apiguardian) {
		because("it's required to generate API tables")
	}

	testImplementation(libs.classgraph) {
		because("ApiReportGenerator needs it")
	}

	testImplementation(libs.jimfs) {
		because("Jimfs is used in src/test/java")
	}

	standaloneConsoleLauncher(projects.junitPlatformConsoleStandalone)
}

asciidoctorj {
	modules {
		diagram.use()
		pdf.version(libs.versions.asciidoctor.pdf)
	}
}

val snapshot = rootProject.version.toString().contains("SNAPSHOT")
val docsVersion = if (snapshot) "snapshot" else rootProject.version
val releaseBranch = if (snapshot) "HEAD" else "r${rootProject.version}"
val docsDir = file("$buildDir/ghpages-docs")
val replaceCurrentDocs = buildParameters.documentation.replaceCurrentDocs
val uploadPdfs = !snapshot
val userGuidePdfFileName = "junit-user-guide-${rootProject.version}.pdf"
val ota4jDocVersion = if (libs.versions.opentest4j.get().contains("SNAPSHOT")) "snapshot" else libs.versions.opentest4j.get()
val apiGuardianDocVersion = if (libs.versions.apiguardian.get().contains("SNAPSHOT")) "snapshot" else libs.versions.apiguardian.get()

gitPublish {
	repoUri.set("https://github.com/junit-team/junit5.git")
	branch.set("gh-pages")
	sign.set(false)
	fetchDepth.set(1)

	contents {
		from(docsDir)
		into("docs")
	}

	preserve {
		include("**/*")
		exclude("docs/$docsVersion/**")
		if (replaceCurrentDocs) {
			exclude("docs/current/**")
		}
	}
}

val generatedAsciiDocPath = layout.buildDirectory.dir("generated/asciidoc")
val consoleLauncherOptionsFile = generatedAsciiDocPath.map { it.file("console-launcher-options.txt") }
val consoleLauncherDiscoverOptionsFile = generatedAsciiDocPath.map { it.file("console-launcher-discover-options.txt") }
val consoleLauncherExecuteOptionsFile = generatedAsciiDocPath.map { it.file("console-launcher-execute-options.txt") }
val consoleLauncherEnginesOptionsFile = generatedAsciiDocPath.map { it.file("console-launcher-engines-options.txt") }
val experimentalApisTableFile = generatedAsciiDocPath.map { it.file("experimental-apis-table.adoc") }
val deprecatedApisTableFile = generatedAsciiDocPath.map { it.file("deprecated-apis-table.adoc") }
val standaloneConsoleLauncherShadowedArtifactsFile = generatedAsciiDocPath.map { it.file("console-launcher-standalone-shadowed-artifacts.adoc") }

val jdkJavadocBaseUrl = "https://docs.oracle.com/en/java/javase/11/docs/api"
val elementListsDir = file("$buildDir/elementLists")
val externalModulesWithoutModularJavadoc = mapOf(
		"org.apiguardian.api" to "https://apiguardian-team.github.io/apiguardian/docs/$apiGuardianDocVersion/api/",
		"org.assertj.core" to "https://javadoc.io/doc/org.assertj/assertj-core/${libs.versions.assertj.get()}/",
		"org.opentest4j" to "https://ota4j-team.github.io/opentest4j/docs/$ota4jDocVersion/api/"
)
require(externalModulesWithoutModularJavadoc.values.all { it.endsWith("/") }) {
	"all base URLs must end with a trailing slash: $externalModulesWithoutModularJavadoc"
}

tasks {

	val consoleLauncherTest by registering(RunConsoleLauncher::class) {
		args.addAll("execute")
		args.addAll("--scan-classpath")
		args.addAll("--config=junit.platform.reporting.open.xml.enabled=true")
		val reportsDir = project.layout.buildDirectory.dir("console-launcher-test-results")
		outputs.dir(reportsDir)
		argumentProviders.add(CommandLineArgumentProvider {
			listOf(
				"--reports-dir=${reportsDir.get()}",
				"--config=junit.platform.reporting.output.dir=${reportsDir.get()}"

			)
		})
		args.addAll("--config", "enableHttpServer=true")
		args.addAll("--include-classname", ".*Tests")
		args.addAll("--include-classname", ".*Demo")
		args.addAll("--exclude-tag", "exclude")
		args.addAll("--exclude-tag", "timeout")
	}

	register<RunConsoleLauncher>("consoleLauncher") {
		hideOutput.set(false)
		outputs.upToDateWhen { false }
	}

	test {
		include("**/*Demo.class")
		(options as JUnitPlatformOptions).apply {
			includeEngines("junit-vintage")
			includeTags("timeout")
		}
	}

	check {
		dependsOn(consoleLauncherTest)
	}

	val generateConsoleLauncherOptions by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("--help", "--disable-banner")
		redirectOutput(consoleLauncherOptionsFile)
	}

	val generateConsoleLauncherDiscoverOptions by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("discover", "--help", "--disable-banner")
		redirectOutput(consoleLauncherDiscoverOptionsFile)
	}

	val generateConsoleLauncherExecuteOptions by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("execute", "--help", "--disable-banner")
		redirectOutput(consoleLauncherExecuteOptionsFile)
	}

	val generateConsoleLauncherEnginesOptions by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("engines", "--help", "--disable-banner")
		redirectOutput(consoleLauncherEnginesOptionsFile)
	}

	val generateExperimentalApisTable by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.api.tools.ApiReportGenerator")
		jvmArgumentProviders += ClasspathSystemPropertyProvider("api.classpath", apiReport)
		args("EXPERIMENTAL")
		redirectOutput(experimentalApisTableFile)
	}

	val generateDeprecatedApisTable by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.api.tools.ApiReportGenerator")
		jvmArgumentProviders += ClasspathSystemPropertyProvider("api.classpath", apiReport)
		args("DEPRECATED")
		redirectOutput(deprecatedApisTableFile)
	}

	val generateStandaloneConsoleLauncherShadowedArtifactsFile by registering(Copy::class) {
		from(zipTree(standaloneConsoleLauncher.elements.map { it.single().asFile })) {
			include("META-INF/shadowed-artifacts")
			includeEmptyDirs = false
			eachFile {
				relativePath = RelativePath(true, standaloneConsoleLauncherShadowedArtifactsFile.get().asFile.name)
			}
			filter { line -> "- `${line}`" }
		}
		into(standaloneConsoleLauncherShadowedArtifactsFile.map { it.asFile.parentFile })
	}

	withType<AbstractAsciidoctorTask>().configureEach {
		inputs.files(
			generateConsoleLauncherOptions,
			generateConsoleLauncherDiscoverOptions,
			generateConsoleLauncherExecuteOptions,
			generateConsoleLauncherEnginesOptions,
			generateExperimentalApisTable,
			generateDeprecatedApisTable,
			generateStandaloneConsoleLauncherShadowedArtifactsFile
		)

		resources {
			from(sourceDir) {
				include("**/images/**/*.png")
				include("**/images/**/*.svg")
			}
		}

		// Temporary workaround for https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/599
		inputs.dir(sourceDir).withPropertyName("sourceDir").withPathSensitivity(RELATIVE)

		attributeProviders += AsciidoctorAttributeProvider {
			mapOf(
				"jupiter-version" to version,
				"platform-version" to project.property("platformVersion"),
				"vintage-version" to project.property("vintageVersion"),
				"bom-version" to version,
				"junit4-version" to libs.versions.junit4.get(),
				"apiguardian-version" to libs.versions.apiguardian.get(),
				"ota4j-version" to libs.versions.opentest4j.get(),
				"surefire-version" to libs.versions.surefire.get(),
				"release-branch" to releaseBranch,
				"docs-version" to docsVersion,
				"revnumber" to version,
				"consoleLauncherOptionsFile" to consoleLauncherOptionsFile.get(),
				"consoleLauncherDiscoverOptionsFile" to consoleLauncherDiscoverOptionsFile.get(),
				"consoleLauncherExecuteOptionsFile" to consoleLauncherExecuteOptionsFile.get(),
				"consoleLauncherEnginesOptionsFile" to consoleLauncherEnginesOptionsFile.get(),
				"experimentalApisTableFile" to experimentalApisTableFile.get(),
				"deprecatedApisTableFile" to deprecatedApisTableFile.get(),
				"standaloneConsoleLauncherShadowedArtifactsFile" to standaloneConsoleLauncherShadowedArtifactsFile.get(),
				"outdir" to outputDir.absolutePath,
				"source-highlighter" to "rouge",
				"tabsize" to "4",
				"toc" to "left",
				"icons" to "font",
				"sectanchors" to true,
				"idprefix" to "",
				"idseparator" to "-",
				"jdk-javadoc-base-url" to jdkJavadocBaseUrl
			)
		}

		sourceSets["test"].apply {
			attributes(mapOf(
					"testDir" to java.srcDirs.first(),
					"testResourcesDir" to resources.srcDirs.first()
			))
			inputs.dir(java.srcDirs.first())
			inputs.dir(resources.srcDirs.first())
			attributes(mapOf("kotlinTestDir" to kotlin.srcDirs.first()))
			inputs.dir(kotlin.srcDirs.first())
		}

		forkOptions {
			// To avoid warning, see https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/597
			jvmArgs(
				"--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
				"--add-opens", "java.base/java.io=ALL-UNNAMED"
			)
		}
	}

	asciidoctor {
		sources {
			include("**/index.adoc")
		}
		resources {
			from(sourceDir) {
				include("tocbot-*/**")
			}
		}
		attributes(mapOf(
				"linkToPdf" to uploadPdfs,
				"userGuidePdfFileName" to userGuidePdfFileName,
				"releaseNotesUrl" to "../release-notes/index.html#release-notes"
		))
	}

	asciidoctorPdf {
		sources {
			include("user-guide/index.adoc")
		}
		copyAllResources()
		attributes(mapOf("releaseNotesUrl" to "https://junit.org/junit5/docs/$docsVersion/release-notes/"))
	}

	val downloadJavadocElementLists by registering {
		outputs.cacheIf { true }
		outputs.dir(elementListsDir).withPropertyName("elementListsDir")
		inputs.property("externalModulesWithoutModularJavadoc", externalModulesWithoutModularJavadoc)
		doFirst {
			externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
				val resource = resources.text.fromUri("${baseUrl}element-list")
				elementListsDir.resolve(moduleName).apply {
					mkdir()
					resolve("element-list").writeText("module:$moduleName\n${resource.asString()}")
				}
			}
		}
	}

	val aggregateJavadocs by registering(Javadoc::class) {
		dependsOn(modularProjects.map { it.tasks.jar })
		dependsOn(downloadJavadocElementLists)
		group = "Documentation"
		description = "Generates aggregated Javadocs"

		title = "JUnit $version API"

		val additionalStylesheetFile = "src/javadoc/junit-stylesheet.css"
		inputs.file(additionalStylesheetFile)
		val overviewFile = "src/javadoc/junit-overview.html"
		inputs.file(overviewFile)

		options {

			memberLevel = JavadocMemberLevel.PROTECTED
			header = rootProject.description
			encoding = "UTF-8"
			locale = "en"
			overview = overviewFile
			jFlags("-Xmx1g")

			this as StandardJavadocDocletOptions
			splitIndex(true)
			addBooleanOption("Xdoclint:all,-missing", true)
			addBooleanOption("html5", true)
			addMultilineStringsOption("tag").value = listOf(
					"apiNote:a:API Note:",
					"implNote:a:Implementation Note:"
			)

			links(jdkJavadocBaseUrl)
			links("https://junit.org/junit4/javadoc/${libs.versions.junit4.get()}/")
			externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
				linksOffline(baseUrl, "$elementListsDir/$moduleName")
			}

			groups = mapOf(
					"Jupiter" to listOf("org.junit.jupiter*"),
					"Vintage" to listOf("org.junit.vintage*"),
					"Platform" to listOf("org.junit.platform*")
			)
			addStringOption("-add-stylesheet", additionalStylesheetFile)
			use(true)
			noTimestamp(true)

			addStringsOption("-module", ",").value = modularProjects.map { it.javaModuleName }
			val moduleSourcePathOption = addPathOption("-module-source-path")
			moduleSourcePathOption.value = modularProjects.map { it.file("src/module") }
			moduleSourcePathOption.value.forEach { inputs.dir(it) }
			addOption(ModuleSpecificJavadocFileOption("-patch-module", modularProjects.associate {
				it.javaModuleName to files(it.sourceSets.matching { it.name.startsWith("main") }.map { it.allJava.srcDirs }).asPath
			}))
			addStringOption("-add-modules", "info.picocli")
			addOption(ModuleSpecificJavadocFileOption("-add-reads", mapOf(
					"org.junit.platform.console" to "info.picocli",
					"org.junit.platform.reporting" to "org.opentest4j.reporting.events",
					"org.junit.jupiter.params" to "univocity.parsers"
			)))
		}

		source(modularProjects.map { files(it.sourceSets.matching { it.name.startsWith("main") }.map { it.allJava }) })
		classpath = files(modularProjects.map { it.sourceSets.main.get().compileClasspath })

		maxMemory = "1024m"
		destinationDir = file("$buildDir/docs/javadoc")

		doFirst {
			(options as CoreJavadocOptions).modulePath = classpath.files.toList()
		}
	}

	val fixJavadoc by registering(Copy::class) {
		dependsOn(aggregateJavadocs)
		group = "Documentation"
		description = "Fix links to external API specs in the locally aggregated Javadoc HTML files"

		val inputDir = aggregateJavadocs.map { it.destinationDir!! }
		inputs.property("externalModulesWithoutModularJavadoc", externalModulesWithoutModularJavadoc)
		from(inputDir.map { File(it, "element-list") }) {
			// For compatibility with pre JDK 10 versions of the Javadoc tool
			rename { "package-list" }
		}
		from(inputDir) {
			filesMatching("**/*.html") {
				val favicon = "<link rel=\"icon\" type=\"image/png\" href=\"https://junit.org/junit5/assets/img/junit5-logo.png\">"
				filter { line ->
					var result = if (line.startsWith("<head>")) line.replace("<head>", "<head>$favicon") else line
					externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
						result = result.replace("${baseUrl}$moduleName/", baseUrl)
					}
					return@filter result
				}
			}
		}
		into("$buildDir/docs/fixedJavadoc")
	}

	val prepareDocsForUploadToGhPages by registering(Copy::class) {
		dependsOn(fixJavadoc, asciidoctor, asciidoctorPdf)
		outputs.dir(docsDir)

		from("$buildDir/checksum") {
			include("published-checksum.txt")
		}
		from(asciidoctor.map { it.outputDir }) {
			include("user-guide/**")
			include("release-notes/**")
			include("tocbot-*/**")
		}
		if (uploadPdfs) {
			from(asciidoctorPdf.map { it.outputDir }) {
				include("**/*.pdf")
				rename { userGuidePdfFileName }
			}
		}
		from(fixJavadoc.map { it.destinationDir }) {
			into("api")
		}
		into("$docsDir/$docsVersion")
		includeEmptyDirs = false
	}

	val createCurrentDocsFolder by registering(Copy::class) {
		dependsOn(prepareDocsForUploadToGhPages)
		outputs.dir("$docsDir/current")
		onlyIf { replaceCurrentDocs }

		from("$docsDir/$docsVersion")
		into("$docsDir/current")
	}

	val configureGitAuthor by registering {
		dependsOn(gitPublishReset)
		doFirst {
			File(gitPublish.repoDir.get().asFile, ".git/config").appendText("""
				[user]
					name = JUnit Team
					email = team@junit.org
			""".trimIndent())
		}
	}

	gitPublishCopy {
		dependsOn(prepareDocsForUploadToGhPages, createCurrentDocsFolder)
	}

	gitPublishCommit {
		dependsOn(configureGitAuthor)
	}
}

fun JavaExec.redirectOutput(outputFile: Provider<RegularFile>) {
	outputs.file(outputFile)
	val byteStream = ByteArrayOutputStream()
	standardOutput = byteStream
	doLast {
		outputFile.get().asFile.apply {
			Files.createDirectories(parentFile.toPath())
			Files.write(toPath(), byteStream.toByteArray())
		}
	}
}

eclipse {
	classpath {
		plusConfigurations.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
		plusConfigurations.add(projects.junitJupiterParams.dependencyProject.configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitJupiterParams.dependencyProject.configurations["shadowed"])
	}
}
