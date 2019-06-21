plugins {
	`java-library-conventions`
}

description = "JUnit Platform Reporting"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	implementation(platform("com.fasterxml.jackson:jackson-bom:2.9.9"))
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("io.jaegertracing:jaeger-client:0.35.5")

	api(project(":junit-platform-launcher"))
}
