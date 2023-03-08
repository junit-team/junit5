plugins {
	id("org.gradlex.build-parameters") version "1.4.3"
}

group = "junitbuild"

buildParameters {
	pluginId("junitbuild.build-parameters")
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
		string("password") {
			description.set("Password to authenticate with the remote build cache")
			fromEnvironment()
		}
		string("url") {
			description.set("URL to the remote build cache")
			fromEnvironment()
		}
	}
	group("documentation") {
		description.set("Parameters controlling how the documentation is built")
		bool("replaceCurrentDocs") {
			description.set("The documentation that is being deployed will replace what's currently deployed as 'current'")
			defaultValue.set(false)
		}
	}
	group("enterprise") {
		description.set("Parameters controlling Gradle Enterprise features")
		string("accessKey") {
			description.set("The access key used to authenticate with Gradle Enterprise")
			fromEnvironment("GRADLE_ENTERPRISE_ACCESS_KEY")
		}
		group("predictiveTestSelection") {
			bool("enabled") {
				description.set("Whether or not to use Predictive Test Selection for selecting tests to execute")
				defaultValue.set(true)
			}
		}
		group("testDistribution") {
			bool("enabled") {
				description.set("Whether or not to use Test Distribution for executing tests")
				defaultValue.set(false)
				fromEnvironment()
			}
			integer("maxLocalExecutors") {
				description.set("How many local executors to use for executing tests")
				defaultValue.set(1)
			}
			integer("maxRemoteExecutors") {
				description.set("How many remote executors to request for executing tests")
			}
		}
	}
	group("testing") {
		description.set("Testing related parmeters")
		bool("enableJaCoCo") {
			description.set("Enables JaCoCo test coverage reporting")
			defaultValue.set(false)
		}
		bool("enableJFR") {
			description.set("Enables Java Flight Recorder functionality")
			defaultValue.set(false)
		}
		integer("retries") {
			description.set("Configures the number of times failing test are retried")
		}
	}
}

tasks {
	withType<JavaCompile>().configureEach {
		options.release.set(11)
	}
}
