plugins {
	alias(libs.plugins.buildParameters)
}

group = "junitbuild"

buildParameters {
	pluginId("junitbuild.build-parameters")
	bool("ci") {
		description = "Whether or not this build is running in a CI environment"
		defaultValue = false
		fromEnvironment()
	}
	integer("javaToolchainVersion") {
		description = "Defines the Java toolchain version to use for compiling code"
	}
	group("buildCache") {
		string("username") {
			description = "Username to authenticate with the remote build cache"
			fromEnvironment()
		}
		string("password") {
			description = "Password to authenticate with the remote build cache"
			fromEnvironment()
		}
		string("url") {
			description = "URL to the remote build cache"
			fromEnvironment()
		}
	}
	group("documentation") {
		description = "Parameters controlling how the documentation is built"
		bool("replaceCurrentDocs") {
			description = "The documentation that is being deployed will replace what's currently deployed as 'current'"
			defaultValue = false
		}
	}
	group("enterprise") {
		description = "Parameters controlling Gradle Enterprise features"
		group("predictiveTestSelection") {
			bool("enabled") {
				description = "Whether or not to use Predictive Test Selection for selecting tests to execute"
				defaultValue = true
			}
		}
		group("testDistribution") {
			bool("enabled") {
				description = "Whether or not to use Test Distribution for executing tests"
				defaultValue = false
				fromEnvironment()
			}
			integer("maxLocalExecutors") {
				description = "How many local executors to use for executing tests"
				defaultValue = 1
			}
			integer("maxRemoteExecutors") {
				description = "How many remote executors to request for executing tests"
			}
		}
	}
	group("testing") {
		description = "Testing related parameters"
		bool("enableJaCoCo") {
			description = "Enables JaCoCo test coverage reporting"
			defaultValue = false
		}
		bool("enableJFR") {
			description = "Enables Java Flight Recorder functionality"
			defaultValue = false
		}
		integer("retries") {
			description = "Configures the number of times failing test are retried"
		}
	}
	group("publishing") {
		bool("signArtifacts") {
			description = "Sign artifacts before publishing them to Maven repos"
		}
	}
}
