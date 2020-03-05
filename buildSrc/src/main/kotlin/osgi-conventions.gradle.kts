import aQute.bnd.gradle.BundleTaskConvention
import aQute.bnd.gradle.FileSetRepositoryConvention
import aQute.bnd.gradle.Resolve

plugins {
	`java-library`
}

// This task enhances `jar` and `shadowJar` tasks with the bnd
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

			# This tells bnd to ignore classes it finds in `META-INF/versions/`
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

			# Instruct the APIGuardianAnnotations how to operate.
			# See https://bnd.bndtools.org/instructions/export-apiguardian.html
			-export-apiguardian: *;version=${project.version}
		""")

	// Add the convention to the jar task
	convention.plugins["bundle"] = btc

	doLast {
		// Do the actual work putting OSGi stuff in the jar.
		btc.buildBundle()
	}
}

val osgiPropertiesFile = file("$buildDir/verifyOSGiProperties.bndrun")

// Bnd's Resolve task uses a properties file for its configuration. This
// task writes out the properties necessary for it to verify the OSGi
// metadata.
val osgiProperties by tasks.registering(WriteProperties::class) {
	outputFile = osgiPropertiesFile
	property("-standalone", true)
	property("-runee", "JavaSE-${Versions.jvmTarget}")
	property("-runrequires", "osgi.identity;filter:='(osgi.identity=${project.name})'")
	property("-runsystempackages", "jdk.internal.misc,sun.misc")
}

val osgiVerification by configurations.creating {
	extendsFrom(configurations.runtimeClasspath.get())
}

// Bnd's Resolve task is what verifies that a jar can be used in OSGi and
// that its metadata is valid. If the metadata is invalid this task will
// fail.
val verifyOSGi by tasks.registering(Resolve::class) {
	dependsOn(osgiProperties)
	setBndrun(osgiPropertiesFile)
	isReportOptional = false
	withConvention(FileSetRepositoryConvention::class) {

		// By default bnd will use jars found in:
		// 1. project.sourceSets.main.runtimeClasspath
		// 2. project.configurations.archives.artifacts.files
		// to validate the metadata.
		// This adds jars defined in `osgiVerification` also so that bnd
		// can use them to validate the metadata without causing those to
		// end up in the dependencies of those projects.
		bundles(osgiVerification)
	}
}

tasks.check {
	dependsOn(verifyOSGi)
}

// The ${project.description}, for some odd reason, is only available
// after evaluation.
afterEvaluate {
	tasks.withType<Jar>().configureEach {
		convention.findPlugin(BundleTaskConvention::class.java)
				?.bnd("Bundle-Name: ${project.description}")
	}
}
