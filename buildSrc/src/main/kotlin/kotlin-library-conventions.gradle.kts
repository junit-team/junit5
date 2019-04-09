import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("java-library-conventions")
	kotlin("jvm")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = Versions.jvmTarget.toString()
		apiVersion = "1.3"
		languageVersion = "1.3"
	}
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-29823, should be fixed in Kotlin Plugin 1.3.30
configurations.getByName("apiElements") {
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_API_JARS))
	}
}
