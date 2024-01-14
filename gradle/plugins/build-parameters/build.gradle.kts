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
	group("documentation") {
		description = "Parameters controlling how the documentation is built"
		bool("replaceCurrentDocs") {
			description = "The documentation that is being deployed will replace what's currently deployed as 'current'"
			defaultValue = false
		}
	}
	group("junit") {
		group("develocity") {
			description = "Parameters controlling Develocity features"
			group("buildCache") {
				string("server") {
					description =
						"Remote build cache server address (protocol and hostname), e.g. https://eu-build-cache-ge.junit.org"
				}
			}
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
	group("manifest") {
		string("buildTimestamp") {
			description = "Overrides the value of the 'Build-Date' and 'Build-Time' jar manifest entries. Can be set as a String (e.g. '2023-11-05 17:49:13.996+0100') or as seconds since the epoch."
			fromEnvironment("SOURCE_DATE_EPOCH") // see https://reproducible-builds.org/docs/source-date-epoch/
		}
		string("builtBy") {
			description = "Overrides the value of the 'Built-By' jar manifest entry"
		}
		string("createdBy") {
			description = "Overrides the value of the 'Created-By' jar manifest entry"
		}
	}
}
