package junitbuild.graalvm

import org.gradle.api.provider.SetProperty

abstract class NativeImagePropertiesExtension {
    abstract val initializeAtBuildTime: SetProperty<String>
}
