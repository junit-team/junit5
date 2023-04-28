package junitbuild.java

import org.gradle.api.file.ConfigurableFileCollection

abstract class ModuleCompileOptions {
    abstract val modulePath: ConfigurableFileCollection
}
