plugins {
	id("junitbuild.checkstyle-conventions")
}

dependencies {
	checkstyle(dependencyFromLibs("nohttp-checkstyle"))
}

tasks.register<Checkstyle>("checkstyleNohttp") {
	group = "verification"
	description = "Checks for illegal uses of http://"
	classpath = files(configurations.checkstyle)
	config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleNohttp.xml"))
	source = fileTree(layout.projectDirectory) {
		exclude(".git/**", "**/.gradle/**")
		exclude(".idea/**", ".eclipse/**")
		exclude("**/*.class")
		exclude("**/*.hprof")
		exclude("**/*.jar")
		exclude("**/*.jpg", "**/*.png")
		exclude("**/*.jks")
		exclude("**/build/**")
	}
}
