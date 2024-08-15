plugins {
	base
	checkstyle
}

checkstyle {
	toolVersion = requiredVersionFromLibs("checkstyle")
	configDirectory = rootProject.layout.projectDirectory.dir("gradle/config/checkstyle")
}

dependencies {
	checkstyle(dependencyFromLibs("nohttp-checkstyle"))
}

tasks {
	val checkstyleNohttp by registering(Checkstyle::class) {
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
	check {
		dependsOn(checkstyleNohttp)
	}
}
