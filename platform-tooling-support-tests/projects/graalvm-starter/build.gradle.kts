plugins {
	java
	id("org.graalvm.buildtools.native")
}

val junitVersion: String by project

dependencies {
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
	testImplementation("junit:junit:4.13.2")
	testImplementation("org.junit.platform:junit-platform-suite:$junitVersion")
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$junitVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-reporting:$junitVersion")
}

tasks.test {
	useJUnitPlatform {
		includeEngines("junit-platform-suite")
	}

	val outputDir = reports.junitXml.outputLocation
	jvmArgumentProviders += CommandLineArgumentProvider {
		listOf(
			"-Djunit.platform.reporting.open.xml.enabled=true",
			"-Djunit.platform.reporting.output.dir=${outputDir.get().asFile.absolutePath}"
		)
	}
}

val initializeAtBuildTime = mapOf(
	// These will be part of the next version of native-build-tools
	// see https://github.com/graalvm/native-build-tools/pull/693
	"5.13" to listOf(
		"org.junit.jupiter.api.DisplayNameGenerator\$IndicativeSentences",
		"org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$ClassInfo",
		"org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor\$LifecycleMethods",
		"org.junit.jupiter.engine.descriptor.ClassTemplateInvocationTestDescriptor",
		"org.junit.jupiter.engine.descriptor.ClassTemplateTestDescriptor",
		"org.junit.jupiter.engine.descriptor.DynamicDescendantFilter\$Mode",
		"org.junit.jupiter.engine.descriptor.ExclusiveResourceCollector\$1",
		"org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor\$MethodInfo",
		"org.junit.jupiter.engine.discovery.ClassSelectorResolver\$DummyClassTemplateInvocationContext",
		"org.junit.platform.engine.support.store.NamespacedHierarchicalStore\$EvaluatedValue",
		"org.junit.platform.launcher.core.DiscoveryIssueNotifier",
		"org.junit.platform.launcher.core.HierarchicalOutputDirectoryProvider",
		"org.junit.platform.launcher.core.LauncherDiscoveryResult\$EngineResultInfo",
		"org.junit.platform.launcher.core.LauncherPhase",
		"org.junit.platform.suite.engine.DiscoverySelectorResolver",
		"org.junit.platform.suite.engine.SuiteTestDescriptor\$DiscoveryIssueForwardingListener",
		"org.junit.platform.suite.engine.SuiteTestDescriptor\$LifecycleMethods",
	),
	// These need to be added to native-build-tools
	"6.0" to listOf(
		"org.junit.platform.commons.util.KotlinReflectionUtils",
		"org.junit.platform.launcher.core.DiscoveryIssueNotifier\$1",
	)
)

graalvmNative {
	binaries {
		named("test") {
			buildArgs.add("-H:+ReportExceptionStackTraces")
			buildArgs.add("--initialize-at-build-time=${initializeAtBuildTime.values.flatten().joinToString(",")}")
		}
	}
}
