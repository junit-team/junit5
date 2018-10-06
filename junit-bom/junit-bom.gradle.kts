description = "${rootProject.description} (Bill of Materials)"

apply(from = "$rootDir/gradle/publishing.gradle")

the<PublishingExtension>().publications.named<MavenPublication>("maven") {
	pom {
		packaging = "pom"
		description.set("This Bill of Materials POM can be used to ease dependency management " +
				"when referencing multiple JUnit artifacts using Gradle or Maven.")
		withXml {
			asElement().apply {
				getElementsByTagName("dependencies")
						.let { children -> (0 until children.length).map { children.item(it) } }
						.forEach { removeChild(it) }
				appendChild(ownerDocument.createElement("dependencyManagement")).apply {
					appendChild(ownerDocument.createElement("dependencies")).apply {
						val mavenizedProjects = rootProject.extra["mavenizedProjects"] as List<String>
						mavenizedProjects.sorted()
								.filter { name -> name != "junit-platform-console-standalone" }
								.map { name -> rootProject.project(name) }
								.forEach { project ->
									appendChild(ownerDocument.createElement("dependency")).apply {
										appendChild(ownerDocument.createElement("groupId")).textContent = project.group as String
										appendChild(ownerDocument.createElement("artifactId")).textContent = project.name
										appendChild(ownerDocument.createElement("version")).textContent = project.version as String
									}
								}
					}
				}
			}
		}
	}
}
