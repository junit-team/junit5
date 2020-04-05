plugins {
	`java-library`
}

val junit_4_12 by configurations.creating {
	extendsFrom(configurations.testRuntimeClasspath.get())
}

dependencies {
	junit_4_12("junit:junit") {
		version {
			strictly("4.12")
		}
	}
	pluginManager.withPlugin("osgi-conventions") {
		"osgiVerification"("org.apache.servicemix.bundles:org.apache.servicemix.bundles.junit:4.13_1")
	}
}

tasks {
	val test_4_12 by registering(Test::class) {
		classpath -= configurations.testRuntimeClasspath.get()
		classpath += junit_4_12
	}
	check {
		dependsOn(test_4_12)
	}
}
