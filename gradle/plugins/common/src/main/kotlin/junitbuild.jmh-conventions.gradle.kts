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

tasks.jmhJar {
	notCompatibleWithConfigurationCache("https://github.com/melix/jmh-gradle-plugin/issues/229")
}

pluginManager.withPlugin("checkstyle") {
	tasks.named<Checkstyle>("checkstyleJmh").configure {
		// use same style rules as defined for tests
		config = resources.text.fromFile(project.the<CheckstyleExtension>().configDirectory.file("checkstyleTest.xml"))
	}
}
