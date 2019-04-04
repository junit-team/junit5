plugins {
	`java-library-conventions`
	id("org.moditect.gradleplugin")
}

description = "JUnit Platform Suite API"

javaLibrary {
	automaticModuleName = "org.junit.platform.suite.api"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
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
