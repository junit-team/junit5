plugins {
	`java-platform`
}

javaPlatform {
	allowDependencies()
}

dependencies {
	api(platform("org.codehaus.groovy:groovy-bom:${versions["groovy"]}"))
	constraints {
		// api means "the dependency is for both compilation and runtime"
		// runtime means "the dependency is only for runtime, not for compilation"
		// In other words, marking dependency as "runtime" would avoid accidental
		// dependency on it during compilation
		runtime("org.apache.logging.log4j:log4j-core:${versions["log4j"]}")
		runtime("org.apache.logging.log4j:log4j-jul:${versions["log4j"]}")
		api("io.github.classgraph:classgraph:${versions["classgraph"]}")
		api("org.openjdk.jmh:jmh-core:${versions["jmh"]}")
		api("org.openjdk.jmh:jmh-generator-annprocess:${versions["jmh"]}")
		api("de.sormuras:bartholdy:${versions["bartholdy"]}")
		api("commons-io:commons-io:${versions["commons-io"]}")
		api("com.tngtech.archunit:archunit-junit5-api:${versions["archunit"]}")
		api("com.tngtech.archunit:archunit-junit5-engine:${versions["archunit"]}")
		api("org.slf4j:slf4j-jdk14:${versions["slf4j"]}")
		api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["kotlinx-coroutines-core"]}")
		api("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
		api("biz.aQute.bnd:biz.aQute.bndlib:${versions["bnd"]}")
		api("org.spockframework:spock-core:${versions["spock"]}")
		api("com.github.gunnarmorling:jfrunit:${versions["jfrunit"]}")
		api("org.jooq:joox:${versions["joox"]}")
	}
}
