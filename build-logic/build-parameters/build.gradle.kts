plugins {
	id("org.gradlex.build-parameters") version "1.4.1"
}

group = "org.junit.gradle"

buildParameters {
	bool("ci") {
		description.set("Whether or not this build is running a CI environment")
		defaultValue.set(false)
		fromEnvironment()
	}
	group("buildCache") {
		string("username") {
			description.set("Username to authenticate with the remote build cache")
			fromEnvironment()
		}
		string("Password") {
			description.set("Password to authenticate with the remote build cache")
			fromEnvironment()
		}
		string("url") {
			description.set("URL to the remote build cache")
			fromEnvironment()
		}
	}
	group("enterprise") {
		description.set("Parameters controlling Gradle Enterprise features")
		bool("enableTestDistribution") {
			description.set("Whether or not to use Test Distribution for executing tests")
			defaultValue.set(false)
			fromEnvironment()
		}
	}
}