plugins {
	`java-platform`
}

dependencies {
	constraints {
		api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
		api("org.opentest4j:opentest4j:${Versions.ota4j}")
		api("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
		api("org.apache.logging.log4j:log4j-jul:${Versions.log4j}")
		api("io.github.classgraph:classgraph:${Versions.classgraph}")
		api("org.codehaus.groovy:groovy-all:${Versions.groovy}")
		api("junit:junit:[${Versions.junit4Min},)") {
			version {
				prefer(Versions.junit4)
			}
		}
		api("com.univocity:univocity-parsers:${Versions.univocity}")
		api("info.picocli:picocli:${Versions.picocli}")
		api("org.assertj:assertj-core:${Versions.assertJ}")
		api("org.openjdk.jmh:jmh-core:${Versions.jmh}")
		api("org.openjdk.jmh:jmh-generator-annprocess:${Versions.jmh}")
		api("de.sormuras:bartholdy:${Versions.bartholdy}")
		api("commons-io:commons-io:${Versions.commonsIo}")
		api("com.tngtech.archunit:archunit-junit5-api:${Versions.archunit}")
		api("com.tngtech.archunit:archunit-junit5-engine:${Versions.archunit}")
		api("org.slf4j:slf4j-jdk14:${Versions.slf4j}")
	}
}
