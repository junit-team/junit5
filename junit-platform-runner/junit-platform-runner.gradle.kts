plugins {
	`java-library-conventions`
	id("org.moditect.gradleplugin")
}

description = "JUnit Platform Runner"

javaLibrary {
	automaticModuleName = "org.junit.platform.runner"
}

dependencies {
	api("junit:junit:${Versions.junit4}")
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
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
