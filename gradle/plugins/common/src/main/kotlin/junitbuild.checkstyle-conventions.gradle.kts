plugins {
	base
	checkstyle
}

checkstyle {
	toolVersion = requiredVersionFromLibs("checkstyle")
	configDirectory = rootProject.layout.projectDirectory.dir("gradle/config/checkstyle")
}

tasks.check {
	dependsOn(tasks.withType<Checkstyle>())
}
