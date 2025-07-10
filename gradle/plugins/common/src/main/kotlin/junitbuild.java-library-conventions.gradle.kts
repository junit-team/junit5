import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import junitbuild.extensions.dependencyFromLibs
import junitbuild.extensions.isSnapshot
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.Library
import org.gradle.plugins.ide.eclipse.model.ProjectDependency
import org.gradle.plugins.ide.eclipse.model.SourceFolder

plugins {
	`java-library`
	eclipse
	idea
	id("junitbuild.base-conventions")
	id("junitbuild.build-parameters")
	id("junitbuild.checkstyle-conventions")
	id("junitbuild.jacoco-java-conventions")
	id("org.openrewrite.rewrite")
}

/**
 * activeRecipe("org.openrewrite.java.RemoveUnusedImports") // there are (non); fails to analyze
 * activeRecipe("org.openrewrite.java.format.AutoFormat") // could remove check, spot, and all other recipes if working
 * activeRecipe("org.openrewrite.java.format.BlankLines") // IndexOutOfBoundsException: Index 0 out of bounds for length 0
 * activeRecipe("org.openrewrite.java.format.NormalizeFormat")
 * activeRecipe("org.openrewrite.java.format.NormalizeLineBreaks")
 * activeRecipe("org.openrewrite.java.format.RemoveTrailingWhitespace")
 * activeRecipe("org.openrewrite.java.format.Spaces")
 * activeRecipe("org.openrewrite.java.format.TabsAndIndents")
 * activeRecipe("org.openrewrite.java.format.WrappingAndBraces")
 * activeRecipe("org.openrewrite.java.migrate.UpgradeToJava17")
 * activeRecipe("org.openrewrite.java.testing.assertj.Assertj")
 * activeRecipe("org.openrewrite.java.testing.cleanup.AssertTrueNullToAssertNull")
 * activeRecipe("org.openrewrite.java.testing.cleanup.TestsShouldNotBePublic")
 * activeRecipe("org.openrewrite.java.testing.junit5.JUnit5BestPractices") // exclude legacy
 * activeRecipe("org.openrewrite.staticanalysis.CodeCleanup")
 * activeRecipe("org.openrewrite.staticanalysis.CommonStaticAnalysis")
 * activeRecipe("org.openrewrite.staticanalysis.FinalizeLocalVariables")
 * activeRecipe("org.openrewrite.staticanalysis.RedundantFileCreation")
 * activeRecipe("org.openrewrite.staticanalysis.RemoveUnusedLocalVariables")
 * activeRecipe("org.openrewrite.staticanalysis.RemoveUnusedPrivateFields")
 * activeRecipe("org.openrewrite.staticanalysis.RemoveUnusedPrivateMethods") // there are non; fails to analyze (even tho it worked once)
 * activeRecipe("org.openrewrite.staticanalysis.StringLiteralEquality")
 * activeRecipe("org.openrewrite.text.EndOfLineAtEndOfFile")
 * activeRecipe("org.openrewrite.java.migrate.UpgradeToJava21")
 */
rewrite {
	activeRecipe("org.openrewrite.staticanalysis.EqualsAvoidsNull")
	activeRecipe("org.openrewrite.staticanalysis.MissingOverrideAnnotation")
	activeRecipe("org.openrewrite.staticanalysis.ModifierOrder")
	failOnDryRunResults = true
}

/**
 * rewrite("org.openrewrite.recipe:rewrite-testing-frameworks")
 * rewrite("org.openrewrite.recipe:rewrite-migrate-java")
 */
dependencies {
	rewrite(platform(dependencyFromLibs("openrewrite-recipe-bom")))
	rewrite("org.openrewrite.recipe:rewrite-static-analysis")
}

val mavenizedProjects: List<Project> by rootProject.extra
val buildDate: String by rootProject.extra
val buildTime: String by rootProject.extra
val buildRevision: Any by rootProject.extra

val extension = extensions.create<JavaLibraryExtension>("javaLibrary")

eclipse {
	jdt {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
		file {
			// Set properties for org.eclipse.jdt.core.prefs
			withProperties {
				// Configure Eclipse projects with -release compiler flag.
				setProperty("org.eclipse.jdt.core.compiler.release", "enabled")
				// Configure Eclipse projects with -parameters compiler flag.
				setProperty("org.eclipse.jdt.core.compiler.codegen.methodParameters", "generate")
			}
		}
	}
	classpath.file.whenMerged {
		this as Classpath
		// Remove classpath entries for non-existent libraries added by various
		// plugins, such as "junit-jupiter-api/build/classes/kotlin/testFixtures".
		entries.removeIf { it is Library && !file(it.path).exists() }
		// Remove classpath entries for the code generator model used by the
		// Java Template Engine (JTE) which is used to generate the JRE enum and
		// dependent tests.
		entries.removeIf { it is ProjectDependency && it.path.equals("/code-generator-model") }
		// Remove classpath entries for anything used by the Gradle Wrapper.
		entries.removeIf { it is Library && it.path.contains("gradle/wrapper") }
		entries.filterIsInstance<SourceFolder>().forEach {
			it.excludes.add("**/module-info.java")
		}
		entries.filterIsInstance<ProjectDependency>().forEach {
			it.entryAttributes.remove("module")
		}
		entries.filterIsInstance<Library>().forEach {
			it.entryAttributes.remove("module")
		}
	}
}

java {
	modularity.inferModulePath = true
}

if (project in mavenizedProjects) {

	apply(plugin = "junitbuild.publishing-conventions")
	apply(plugin = "junitbuild.osgi-conventions")

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
				addBooleanOption("Xdoclint:all,-missing,-reference", true)
				addBooleanOption("XD-Xlint:none", true)
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
					description = provider { "Module \"${project.name}\" of JUnit" }
				}
			}
		}
	}

	if (!project.version.isSnapshot()) {
		configurations {
			compileClasspath {
				resolutionStrategy.failOnChangingVersions()
			}
			runtimeClasspath {
				resolutionStrategy.failOnChangingVersions()
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
	dirPermissions {
		unix("rwxr-xr-x")
	}
	filePermissions {
		unix("rw-r--r--")
	}
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

tasks.withType<Jar>().configureEach {
	from(rootDir) {
		include("LICENSE.md")
		into("META-INF")
	}
	from(rootDir) {
		include("NOTICE.md")
		rename {
			"LICENSE-notice.md"
		}
		into("META-INF")
	}
}

tasks.jar {
	manifest {
		attributes(
				"Created-By" to (buildParameters.manifest.createdBy.orNull
					?: "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})"),
				"Built-By" to buildParameters.manifest.builtBy.orElse("JUnit Team"),
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

tasks.withType<ShadowJar>().configureEach {
	outputs.doNotCacheIf("Shadow jar contains a Manifest with Build-Time") { true }
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.compilerArgs.addAll(listOf(
		"-Xlint:all", // Enables all recommended warnings.
		"-Werror", // Terminates compilation when warnings occur.
		"-parameters", // Generates metadata for reflection on method parameters.
	))
}

tasks.compileJava {
	options.compilerArgs.addAll(listOf(
		"--module-version", "${project.version}"
	))
}

configurations {
	apiElements {
		attributes {
			attributeProvider(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.map { it.majorVersion.toInt() })
		}
	}
	runtimeElements {
		attributes {
			attributeProvider(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, extension.mainJavaVersion.map { it.majorVersion.toInt() })
		}
	}
}

tasks {
	compileJava {
		options.release = extension.mainJavaVersion.map { it.majorVersion.toInt() }
	}
	compileTestJava {
		options.release = extension.testJavaVersion.map { it.majorVersion.toInt() }
	}
}

afterEvaluate {
	pluginManager.withPlugin("groovy") {
		tasks.named<GroovyCompile>("compileGroovy").configure {
			// Groovy compiler does not support the --release flag.
			sourceCompatibility = extension.mainJavaVersion.get().majorVersion
			targetCompatibility = extension.mainJavaVersion.get().majorVersion
		}
		tasks.withType<GroovyCompile>().named { it.startsWith("compileTest") }.configureEach {
			// Groovy compiler does not support the --release flag.
			sourceCompatibility = extension.testJavaVersion.get().majorVersion
			targetCompatibility = extension.testJavaVersion.get().majorVersion
		}
	}
}

tasks {
	checkstyleMain {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleMain.xml"))
	}
	checkstyleTest {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleTest.xml"))
	}
	check {
		dependsOn(rewriteDryRun)
	}
}

pluginManager.withPlugin("java-test-fixtures") {
	tasks.named<Checkstyle>("checkstyleTestFixtures") {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleTest.xml"))
	}
	tasks.named<JavaCompile>("compileTestFixturesJava") {
		options.release = extension.testJavaVersion.map { it.majorVersion.toInt() }
	}
}
