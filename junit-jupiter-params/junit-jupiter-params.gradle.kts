import junitbuild.extensions.javaModuleName
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
	id("junitbuild.java-nullability-conventions")
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.shadow-conventions")
	id("junitbuild.jmh-conventions")
	`java-test-fixtures`
}

description = "JUnit Jupiter Params"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)
	compileOnly(libs.jspecify)

	shadowed(libs.fastcsv)

	compileOnly(kotlin("stdlib"))

	osgiVerification(projects.junitJupiterEngine)
	osgiVerification(projects.junitPlatformLauncher)
}

tasks {
	jar {
		bundle {
			val version = project.version
			bnd("""
				Require-Capability:\
					org.junit.platform.engine;\
						filter:='(&(org.junit.platform.engine=junit-jupiter)(version>=${'$'}{version_cleanup;$version})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;$version}})))';\
						effective:=active
			""")
		}
	}
	val extractFastCSVLicense by registering(Sync::class) {
		from(zipTree(project.configurations.shadowedClasspath.map { it.files.single { file -> file.name.contains("fastcsv") } })) {
			include("META-INF/LICENSE")
		}
		into(layout.buildDirectory.dir("fastcsv"))
	}
	shadowJar {
		val tempLicenseFile = projectDir.resolve("LICENSE-fastcsv")

		relocate("de.siegmar.fastcsv", "org.junit.jupiter.params.shadow.de.siegmar.fastcsv")

		from(projectDir) {
			include(tempLicenseFile.name)
			into("META-INF")
		}

		from(extractFastCSVLicense.map { it.destinationDir }) {
			rename { "LICENSE-fastcsv" }
		}
	}
	compileJava {
		options.compilerArgs.addAll(listOf(
			"--add-modules", "de.siegmar.fastcsv",
			"--add-reads", "${javaModuleName}=de.siegmar.fastcsv"
		))
	}
	compileJmhJava {
		options.compilerArgs.add("-Xlint:-processing")
		options.errorprone.nullaway {
			customInitializerAnnotations.add(
				"org.openjdk.jmh.annotations.Setup",
			)
		}
	}
	javadoc {
		(options as StandardJavadocDocletOptions).apply {
			addStringOption("-add-modules", "de.siegmar.fastcsv")
			addStringOption("-add-reads", "${javaModuleName}=de.siegmar.fastcsv")
		}
	}
}
