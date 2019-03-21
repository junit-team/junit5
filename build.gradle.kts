import org.ajoberstar.gradle.git.publish.GitPublishExtension
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.spi.ToolProvider

plugins {
	`java-library`
	kotlin("jvm")
	checkstyle
	eclipse
	idea
	id("com.gradle.build-scan")
	id("net.nemerosa.versioning")
	id("com.github.ben-manes.versions") apply false
	id("com.diffplug.gradle.spotless")
	id("org.ajoberstar.git-publish")
	id("de.marcphilipp.nexus-publish") apply false
}

buildScan {
	termsOfServiceUrl = "https://gradle.com/terms-of-service"
	termsOfServiceAgree = "yes"
}

val buildTimeAndDate = OffsetDateTime.now()
val buildDate by extra { DateTimeFormatter.ISO_LOCAL_DATE.format(buildTimeAndDate) }
val buildTime by extra { DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ").format(buildTimeAndDate) }
val buildRevision by extra { versioning.info.commit }
val builtByValue by extra { project.findProperty("builtBy") ?: project.property("defaultBuiltBy") }

val docsVersion by extra { if (project.version.toString().contains("SNAPSHOT")) "snapshot" else project.version }

val platformProjects by extra(listOf(
		project(":junit-platform-commons"),
		project(":junit-platform-console"),
		project(":junit-platform-console-standalone"),
		project(":junit-platform-engine"),
		project(":junit-platform-launcher"),
		project(":junit-platform-reporting"),
		project(":junit-platform-runner"),
		project(":junit-platform-suite-api"),
		project(":junit-platform-testkit")
))

val jupiterProjects by extra(listOf(
		project(":junit-jupiter"),
		project(":junit-jupiter-api"),
		project(":junit-jupiter-engine"),
		project(":junit-jupiter-migrationsupport"),
		project(":junit-jupiter-params")
))

val vintageProjects by extra(listOf(
		project(":junit-vintage-engine")
))

val mavenizedProjects by extra(platformProjects + jupiterProjects + vintageProjects)

val license by extra(License(
		name = "Eclipse Public License v2.0",
		url = uri("http://www.eclipse.org/legal/epl-v20.html"),
		headerFile = file("src/spotless/eclipse-public-license-2.0.java")
))

val enableJaCoCo = project.hasProperty("enableJaCoCo")
val jacocoTestProjects = listOf(
		project(":junit-jupiter-engine"),
		project(":junit-jupiter-migrationsupport"),
		project(":junit-jupiter-params"),
		project(":junit-vintage-engine"),
		project(":platform-tests")
)
val jacocoCoveredProjects = mavenizedProjects - listOf(project(":junit-platform-console-standalone"))
val jacocoClassesDir = file("$buildDir/jacoco/classes")

allprojects {

	apply(plugin = "java-library")
	apply(plugin = "kotlin")
	apply(plugin = "eclipse")
	apply(plugin = "idea")
	apply(plugin = "com.diffplug.gradle.spotless")
	apply(plugin = "checkstyle")
	apply(plugin = "com.github.ben-manes.versions") // gradle dependencyUpdates

	if (enableJaCoCo) {
		apply(plugin = "jacoco")
		configure<JacocoPluginExtension> {
			toolVersion = Versions.jacoco
		}
	}

	repositories {
		// mavenLocal()
		mavenCentral()
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
	}

	tasks.compileJava {
		options.encoding = "UTF-8"

		// See: https://docs.oracle.com/en/java/javase/11/tools/javac.html
		options.compilerArgs.addAll(listOf(
			"-Xlint", // Enables all recommended warnings.
			"-Werror" // Terminates compilation when warnings occur.
		))
	}

	// Declare "mainJavaVersion" and "testJavaVersion" as a global properties
	//  so they can be overridden by subprojects
	val mainJavaVersion by extra(Versions.jvmTarget)
	val testJavaVersion by extra(JavaVersion.VERSION_11)

	afterEvaluate {
		tasks {
			compileJava {
				sourceCompatibility = mainJavaVersion.majorVersion
				targetCompatibility = mainJavaVersion.majorVersion // needed by asm
				// --release release
				// Compiles against the public, supported and documented API for a specific VM version.
				// Supported release targets are 6, 7, 8, 9, 10, and 11.
				// Note that if --release is added then -target and -source are ignored.
				options.compilerArgs.addAll(listOf("--release", mainJavaVersion.majorVersion))
			}
			compileTestJava {
				options.encoding = "UTF-8"
				sourceCompatibility = testJavaVersion.majorVersion
				targetCompatibility = testJavaVersion.majorVersion

				// See: https://docs.oracle.com/en/java/javase/11/tools/javac.html
				options.compilerArgs.addAll(listOf(
						"-Xlint", // Enables all recommended warnings.
						"-Xlint:-overrides", // Disables "method overrides" warnings.
						"-parameters" // Generates metadata for reflection on method parameters.
				))
			}
		}
	}

	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		kotlinOptions {
			jvmTarget = Versions.jvmTarget.toString()
			apiVersion = "1.1"
			languageVersion = "1.1"
		}
	}

	// Workaround for https://youtrack.jetbrains.com/issue/KT-29823, should be fixed in Kotlin Plugin 1.3.30
	configurations.getByName("apiElements") {
		attributes {
			attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_API_JARS))
		}
	}

	checkstyle {
		toolVersion = Versions.checkstyle
		configDir = rootProject.file("src/checkstyle")
	}
	tasks {
		checkstyleMain {
			configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
		}
		checkstyleTest {
			configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
		}
	}
}

subprojects {

	if (project in jupiterProjects) {
		group = property("jupiterGroup")!!
	}
	else if (project in platformProjects) {
		group = property("platformGroup")!!
		version = property("platformVersion")!!
	}
	else if (project in vintageProjects) {
		group = property("vintageGroup")!!
		version = property("vintageVersion")!!
	}

	val shadowed by configurations.creating

	sourceSets {
		main {
			compileClasspath += shadowed
		}
		test {
			runtimeClasspath += shadowed
		}
	}

	eclipse {
		classpath {
			plusConfigurations.add(shadowed)
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

		apply(from = "$rootDir/gradle/publishing.gradle.kts")

		tasks.javadoc {
			options {
				memberLevel = JavadocMemberLevel.PROTECTED
				header = project.name
				encoding = "UTF-8"
				(this as StandardJavadocDocletOptions).apply {
					addBooleanOption("Xdoclint:html,syntax", true)
					addBooleanOption("html5", true)
					addBooleanOption("-no-module-directories", true)
					addMultilineStringsOption("tag").value = listOf(
							"apiNote:a:API Note:",
							"implNote:a:Implementation Note:"
					)
					use(true)
					noTimestamp(true)
				}
			}
		}

		val sourcesJar by tasks.creating(Jar::class) {
			dependsOn(tasks.classes)
			archiveClassifier.set("sources")
			from(sourceSets.main.get().allSource)
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		}

		val javadocJar by tasks.creating(Jar::class) {
			archiveClassifier.set("javadoc")
			from(tasks.javadoc)
		}

		tasks.withType<Jar> {
			from(rootDir) {
				include("LICENSE.md", "LICENSE-notice.md")
				into("META-INF")
			}
		}

		configure<PublishingExtension> {
			publications {
				named<MavenPublication>("maven") {
					from(components["java"])
					artifact(sourcesJar)
					artifact(javadocJar)
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

		// If available, compile and include classes for other Java versions.
		listOf("9").forEach { version ->
			val versionedProject = findProject(":${project.name}-java-$version")
			if (versionedProject != null) {
				// We"re only interested in the compiled classes. So we depend
				// on the classes task and change (-C) to the destination
				// directory of the version-aware project later.
				dependsOn(versionedProject.tasks.classes)
				doLast {
					ToolProvider.findFirst("jar").get().run(System.out, System.err,
							"--update",
							"--file", archivePath.toString(),
							"--release", version,
							"-C", versionedProject.tasks.compileJava.get().destinationDir.toString(),
							".")
				}
			}
		}
	}

	spotless {
		val headerFile = license.headerFile
		val importOrderConfigFile = rootProject.file("src/eclipse/junit-eclipse.importorder")
		val javaFormatterConfigFile = rootProject.file("src/eclipse/junit-eclipse-formatter-settings.xml")

		java {
			licenseHeaderFile(headerFile, "(package|import|open|module) ")
			importOrderFile(importOrderConfigFile)
			eclipse().configFile(javaFormatterConfigFile)
			removeUnusedImports()
			trimTrailingWhitespace()
			endWithNewline()
		}

		kotlin {
			ktlint(Versions.ktlint)
			licenseHeaderFile(headerFile)
			trimTrailingWhitespace()
			endWithNewline()
		}
	}

	afterEvaluate {
		if (enableJaCoCo && project in jacocoCoveredProjects) {
			val jarTask = (tasks.findByName("shadowJar") ?: tasks.jar.get()) as Jar
			val extractJar by tasks.registering(Copy::class) {
				from(zipTree(jarTask.archivePath))
				into(jacocoClassesDir)
				include("**/*.class")
				// don"t report coverage for shadowed classes
				exclude("**/shadow/**")
				// don"t version-specific classes of MR JARs
				exclude("META-INF/versions/**")
				includeEmptyDirs = false
				onlyIf { jarTask.enabled }
			}
			jarTask.finalizedBy(extractJar)
		}
	}
}

rootProject.apply {
	apply(plugin = "org.ajoberstar.git-publish")

	description = "JUnit 5"

	val docsDir = file("$buildDir/ghpages-docs")
	val replaceCurrentDocs = project.hasProperty("replaceCurrentDocs")
	val ota4jDocVersion = if (Versions.ota4j.contains("SNAPSHOT")) "snapshot" else Versions.ota4j
	val apiGuardianDocVersion = if (Versions.apiGuardian.contains("SNAPSHOT")) "snapshot" else Versions.apiGuardian

	gitPublish {
		repoUri.set("https://github.com/junit-team/junit5.git")
		branch.set("gh-pages")

		contents {
			from(docsDir)
			into("docs")
		}

		preserve {
			include("**/*")
			exclude("docs/${docsVersion}/**")
			if (replaceCurrentDocs) {
				exclude("docs/current/**")
			}
		}
	}

	tasks {
		jar {
			enabled = false
		}
		val aggregateJavadocs by registering(Javadoc::class) {
			group = "Documentation"
			description = "Generates aggregated Javadocs"
			dependsOn(subprojects.map { it.tasks.named("jar") })

			title = "JUnit ${version} API"

			options {
				memberLevel = JavadocMemberLevel.PROTECTED
				header = rootProject.description
				encoding = "UTF-8"
				(this as StandardJavadocDocletOptions).apply {
					splitIndex(true)
					addBooleanOption("Xdoclint:none", true)
					addBooleanOption("html5", true)
					addBooleanOption("-no-module-directories", true)
					addMultilineStringsOption("tag").value = listOf(
						"apiNote:a:API Note:",
						"implNote:a:Implementation Note:"
					)
					jFlags("-Xmx1g")
					source("8") // https://github.com/junit-team/junit5/issues/1735
					links("https://docs.oracle.com/javase/8/docs/api/")
					links("https://ota4j-team.github.io/opentest4j/docs/${ota4jDocVersion}/api/")
					links("https://apiguardian-team.github.io/apiguardian/docs/${apiGuardianDocVersion}/api/")
					links("https://junit.org/junit4/javadoc/${Versions.junit4}/")
					links("https://joel-costigliola.github.io/assertj/core-8/api/")
					stylesheetFile = rootProject.file("src/javadoc/stylesheet.css")
					groups = mapOf(
						"Jupiter" to listOf("org.junit.jupiter.*"),
						"Vintage" to listOf("org.junit.vintage.*"),
						"Platform" to listOf("org.junit.platform.*")
					)
					use(true)
					noTimestamp(true)
				}
			}
			// Only generate JavaDoc for "main" sources in Mavenized projects
			source(subprojects.filter { it in mavenizedProjects }.map { it.sourceSets.main.get().allJava })

			maxMemory = "1024m"
			destinationDir = file("$buildDir/docs/javadoc")
			classpath = files(subprojects.map { it.sourceSets.main.get().compileClasspath })
				// Remove Kotlin classes from classpath due to "bad" class file
				// see https://bugs.openjdk.java.net/browse/JDK-8187422
				.filter { !it.path.contains("kotlin") }
				// Remove subproject JARs so Kotlin classes don"t get picked up
				.filter { it.isDirectory() || !it.absolutePath.startsWith(projectDir.absolutePath) }

			doLast {
				// For compatibility with pre JDK 10 versions of the Javadoc tool
				copy {
					from(File(destinationDir, "element-list"))
					into(destinationDir)
					rename { "package-list" }
				}
			}
		}

		val prepareDocsForUploadToGhPages by registering(Copy::class) {
			dependsOn(aggregateJavadocs, ":documentation:asciidoctor")
			outputs.dir(docsDir)

			from("${project(":documentation").buildDir}/checksum") {
				include("published-checksum.txt")
			}
			from("${project(":documentation").buildDir}/asciidoc") {
				include("user-guide/**")
				include("release-notes/**")
				include("tocbot-*/**")
			}
			from("$buildDir/docs") {
				include("javadoc/**")
				filesMatching("**/*.html") {
					val favicon = "<link rel=\"icon\" type=\"image/png\" href=\"https://junit.org/junit5/assets/img/junit5-logo.png\">"
					filter { line ->
						if (line.startsWith("<head>")) line.replace("<head>", "<head>$favicon") else line
					}
				}
			}
			into("${docsDir}/${docsVersion}")
			filesMatching("javadoc/**") {
				path = path.replace("javadoc/", "api/")
			}
			includeEmptyDirs = false
		}

		val createCurrentDocsFolder by registering(Copy::class) {
			dependsOn(prepareDocsForUploadToGhPages)
			outputs.dir("${docsDir}/current")
			onlyIf { replaceCurrentDocs }

			from("${docsDir}/${docsVersion}")
			into("${docsDir}/current")
		}

		gitPublishCommit {
			dependsOn(prepareDocsForUploadToGhPages, createCurrentDocsFolder)
		}
	}

	spotless {
		format("misc") {
			target("**/*.gradle", "**/*.gradle.kts", "**/*.gitignore")
			indentWithTabs()
			trimTrailingWhitespace()
			endWithNewline()
		}
		format("documentation") {
			target("**/*.adoc", "**/*.md")
			trimTrailingWhitespace()
			endWithNewline()
		}
	}

	tasks {
		if (enableJaCoCo) {
			val jacocoMerge by registering(JacocoMerge::class) {
				subprojects.filter { it in jacocoTestProjects }
						.forEach { subproj ->
							executionData(fileTree("dir" to "${subproj.buildDir}/jacoco", "include" to "*.exec"))
							dependsOn(subproj.tasks.withType<Test>())
						}
			}
			register<JacocoReport>("jacocoRootReport") {
				dependsOn(jacocoMerge)
				sourceDirectories.from(files(subprojects
						.filter { it in jacocoCoveredProjects }
						.map { it.sourceSets.main.get().allSource.srcDirs }))
				classDirectories.from(files(jacocoClassesDir))
				executionData(jacocoMerge.get().destinationFile)
				reports {
					html.isEnabled = true
					xml.isEnabled = true
					csv.isEnabled = false
				}
			}
		}
	}
}
