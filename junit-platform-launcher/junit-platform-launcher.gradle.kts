plugins {
	`java-library-conventions`
	id("org.moditect.gradleplugin")
}

description = "JUnit Platform Launcher"

javaLibrary {
	automaticModuleName = "org.junit.platform.launcher"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))
}

moditect {
	addMainModuleInfo {
		overwriteExistingFiles.set(true)
		module {
			moduleInfo {
				name = "org." + project.name.replace('-', '.')
			}
		}
	}
}
