plugins {
	java
}

val jupiterVersion: String by project
val platformVersion: String by project

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$platformVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
	}
}

tasks.test {
	useJUnitPlatform()

	testLogging {
		events("failed", "standardOut")
	}

	reports {
		html.required = true
	}
}
