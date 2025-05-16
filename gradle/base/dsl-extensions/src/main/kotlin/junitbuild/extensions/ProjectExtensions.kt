package junitbuild.extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

val Project.javaModuleName: String
    get() = toModuleName(name)

val ProjectDependency.javaModuleName: String
    get() = toModuleName(name)

private fun toModuleName(name: String) = "org.${name.replace('-', '.')}"

fun Project.dependencyProject(dependency: ProjectDependency) =
    project(dependency.path)

fun Project.requiredVersionFromLibs(name: String) =
    libsVersionCatalog.findVersion(name).get().requiredVersion

fun Project.dependencyFromLibs(name: String) =
    libsVersionCatalog.findLibrary(name).get()

fun Project.bundleFromLibs(name: String) =
    libsVersionCatalog.findBundle(name).get()

private val Project.libsVersionCatalog: VersionCatalog
    get() = the<VersionCatalogsExtension>().named("libs")
