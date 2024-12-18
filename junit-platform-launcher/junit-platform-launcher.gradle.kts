plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.native-image-properties")
	`java-test-fixtures`
}

description = "JUnit Platform Launcher"

dependencies {
	api(platform(projects.junitBom))
	api(projects.junitPlatformEngine)

	compileOnlyApi(libs.apiguardian)

	osgiVerification(projects.junitJupiterEngine)
}

nativeImageProperties {
	initializeAtBuildTime.addAll(
		"org.junit.platform.launcher.LauncherSessionListener$1",
		"org.junit.platform.launcher.TestIdentifier",
		"org.junit.platform.launcher.core.InternalTestPlan",
		"org.junit.platform.launcher.core.TestExecutionListenerRegistry",
		"org.junit.platform.launcher.core.EngineDiscoveryOrchestrator",
		"org.junit.platform.launcher.core.LauncherConfig",
		"org.junit.platform.launcher.core.LauncherConfigurationParameters",
		"org.junit.platform.launcher.core.HierarchicalOutputDirectoryProvider",
		"org.junit.platform.launcher.core.DefaultLauncher",
		"org.junit.platform.launcher.core.DefaultLauncherConfig",
		"org.junit.platform.launcher.core.EngineExecutionOrchestrator",
		"org.junit.platform.launcher.core.LauncherConfigurationParameters\$ParameterProvider$1",
		"org.junit.platform.launcher.core.LauncherConfigurationParameters\$ParameterProvider$2",
		"org.junit.platform.launcher.core.LauncherConfigurationParameters\$ParameterProvider$3",
		"org.junit.platform.launcher.core.LauncherConfigurationParameters\$ParameterProvider$4",
		"org.junit.platform.launcher.core.LauncherDiscoveryResult",
		"org.junit.platform.launcher.core.LauncherListenerRegistry",
		"org.junit.platform.launcher.core.ListenerRegistry",
		"org.junit.platform.launcher.core.SessionPerRequestLauncher",
		"org.junit.platform.launcher.listeners.UniqueIdTrackingListener",
	)
}

tasks {
	jar {
		bundle {
			val version = project.version
			bnd("""
				Provide-Capability:\
					org.junit.platform.launcher;\
						org.junit.platform.launcher='junit-platform-launcher';\
						version:Version="${'$'}{version_cleanup;${version}}"
			""")
		}
	}
}
