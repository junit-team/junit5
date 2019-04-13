import java.util.spi.ToolProvider

plugins {
	`java-library-conventions`
}

description = "JUnit Platform Commons"

sourceSets {
	create("mainRelease9") {
		java {
			setSrcDirs(setOf("src/main/java9"))
		}
	}
}

configurations {
	named("mainRelease9CompileClasspath") {
		extendsFrom(compileClasspath.get())
	}
}

dependencies {
	add("mainRelease9Compile", sourceSets.main.get().output)
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}

tasks {

	val compileMainRelease9Java by existing(JavaCompile::class) {
		sourceCompatibility = "9"
		targetCompatibility = "9"
		options.compilerArgs.addAll(listOf("--release", "9"))
	}

	jar {
		dependsOn(compileMainRelease9Java)
		doLast {
			ToolProvider.findFirst("jar").get().run(System.out, System.err, "--update",
					"--file", archiveFile.get().asFile.absolutePath,
					"--release", "9",
					"-C", "$buildDir/classes/java/mainRelease9", ".")
		}
	}

	named<Checkstyle>("checkstyleMainRelease9").configure {
		configFile = rootProject.file("src/checkstyle/checkstyleMain.xml")
	}

}

eclipse {
	classpath {
		sourceSets -= mainRelease9
	}
}
