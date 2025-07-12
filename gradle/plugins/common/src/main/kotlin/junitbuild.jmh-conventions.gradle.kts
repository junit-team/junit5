import junitbuild.extensions.requiredVersionFromLibs
import junitbuild.extensions.dependencyFromLibs

plugins {
	id("me.champeau.jmh")
}

jmh {
	jmhVersion = requiredVersionFromLibs("jmh")
}

dependencies {
	jmh(dependencyFromLibs("jmh-core"))
	jmhAnnotationProcessor(dependencyFromLibs("jmh-generator-annprocess"))
}
