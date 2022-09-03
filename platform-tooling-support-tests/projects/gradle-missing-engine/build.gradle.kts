plugins {
	java
}

// grab jupiter version from system environment
val jupiterVersion: String = System.getenv("JUNIT_JUPITER_VERSION")
val vintageVersion: String = System.getenv("JUNIT_VINTAGE_VERSION")
val platformVersion: String = System.getenv("JUNIT_PLATFORM_VERSION")

// emit default file encoding to a file
file("file.encoding.txt").writeText(System.getProperty("file.encoding"))

// emit more Java runtime information
file("java.runtime.txt").writeText("""
java.version=${System.getProperty("java.version")}
""")

// emit versions of JUnit groups
file("junit.versions.txt").writeText("""
jupiterVersion=$jupiterVersion
vintageVersion=$vintageVersion
platformVersion=$platformVersion
""")

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion") {
		exclude(group = "org.junit.jupiter", module = "junit-jupiter-engine")
	}
}

tasks.test {
	useJUnitPlatform()
}
