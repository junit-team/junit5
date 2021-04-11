plugins {
	`java-platform`
}

dependencies {
	constraints {
		api("org.openjdk.jmh:jmh-core:${versions["jmh"]}")
		api("org.openjdk.jmh:jmh-generator-annprocess:${versions["jmh"]}")
		api("org.slf4j:slf4j-jdk14:${versions["slf4j"]}")
		api("org.mockito:mockito-junit-jupiter:${versions["mockito"]}")
		api("biz.aQute.bnd:biz.aQute.bndlib:${versions["bnd"]}")
		api("org.spockframework:spock-core:${versions["spock"]}")
		api("com.github.gunnarmorling:jfrunit:${versions["jfrunit"]}")
		api("org.jooq:joox:${versions["joox"]}")
	}
}
