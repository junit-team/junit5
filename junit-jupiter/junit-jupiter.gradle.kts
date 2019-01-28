description = "JUnit Jupiter (Aggregator)"

dependencies {
	api(project(":junit-jupiter-api"))
	api(project(":junit-jupiter-params"))
	runtimeOnly(project(":junit-jupiter-engine"))
}

tasks.jar {
	manifest {
		attributes(
				"Automatic-Module-Name" to "org.junit.jupiter"
		)
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			afterEvaluate {
				configurations.all {
					dependencies.removeIf{
						it.group == "org.apiguardian"
					}
				}
			}
		}
	}
}
