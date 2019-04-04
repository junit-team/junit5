plugins {
	`java-library-conventions`
	id("org.moditect.gradleplugin")
}

description = "JUnit Platform Reporting"

javaLibrary {
	automaticModuleName = "org.junit.platform.reporting"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
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
