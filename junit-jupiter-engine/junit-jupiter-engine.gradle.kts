plugins {
	id("junitbuild.kotlin-library-conventions")
	id("junitbuild.native-image-properties")
	`java-test-fixtures`
}

description = "JUnit Jupiter Engine"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)
	api(projects.junitJupiterApi)

	compileOnlyApi(libs.apiguardian)

	osgiVerification(projects.junitPlatformLauncher)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.jupiter.engine.JupiterTestEngine",
		"org.junit.jupiter.engine.config.CachingJupiterConfiguration",
		"org.junit.jupiter.engine.config.DefaultJupiterConfiguration",
		"org.junit.jupiter.engine.config.EnumConfigurationParameterConverter",
		"org.junit.jupiter.engine.config.InstantiatingConfigurationParameterConverter",
		"org.junit.jupiter.engine.descriptor.ClassTestDescriptor",
		"org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor",
		"org.junit.jupiter.engine.descriptor.DynamicDescendantFilter",
		"org.junit.jupiter.engine.descriptor.ExclusiveResourceCollector\$1",
		"org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor",
		"org.junit.jupiter.engine.descriptor.JupiterTestDescriptor",
		"org.junit.jupiter.engine.descriptor.JupiterTestDescriptor$1",
		"org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor",
		"org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor",
		"org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor",
		"org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor",
		"org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor",
		"org.junit.jupiter.engine.execution.ConditionEvaluator",
		"org.junit.jupiter.engine.execution.ExecutableInvoker",
		"org.junit.jupiter.engine.execution.InterceptingExecutableInvoker",
		"org.junit.jupiter.engine.execution.InterceptingExecutableInvoker\$ReflectiveInterceptorCall",
		"org.junit.jupiter.engine.execution.InterceptingExecutableInvoker\$ReflectiveInterceptorCall\$VoidMethodInterceptorCall",
		"org.junit.jupiter.engine.execution.InvocationInterceptorChain",
	)
}

tasks {
	jar {
		bundle {
			val platformVersion: String by rootProject.extra
			bnd("""
				Provide-Capability:\
					org.junit.platform.engine;\
						org.junit.platform.engine='junit-jupiter';\
						version:Version="${'$'}{version_cleanup;${project.version}}"
				Require-Capability:\
					org.junit.platform.launcher;\
						filter:='(&(org.junit.platform.launcher=junit-platform-launcher)(version>=${'$'}{version_cleanup;${platformVersion}})(!(version>=${'$'}{versionmask;+;${'$'}{version_cleanup;${platformVersion}}})))';\
						effective:=active
			""")
		}
	}
}
