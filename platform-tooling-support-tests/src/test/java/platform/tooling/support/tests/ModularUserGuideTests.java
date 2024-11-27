/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.spi.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.DisabledOnOpenJ9;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.launcher.LauncherConstants;

import platform.tooling.support.Helper;
import platform.tooling.support.ThirdPartyJars;
import platform.tooling.support.process.ProcessStarters;

/**
 * @since 1.5
 */
@DisabledOnOpenJ9
class ModularUserGuideTests {

	private static final String DOCUMENTATION_MODULE_DESCRIPTOR = """
			open module documentation {
			  exports example.testkit; // just here to ensure documentation example sources are compiled

			  requires org.junit.jupiter.api;
			  requires org.junit.jupiter.migrationsupport;
			  requires org.junit.jupiter.params;

			  requires org.junit.platform.engine;
			  requires org.junit.platform.reporting;
			  requires org.junit.platform.runner;
			  requires org.junit.platform.testkit;

			  // Byte Buddy is used by AssertJ's soft assertions which are used by the Engine Test Kit
			  requires net.bytebuddy;

			  requires java.desktop;
			  requires java.logging;
			  requires java.scripting;
			  requires jdk.httpserver;

			  provides org.junit.platform.launcher.LauncherSessionListener
			    with example.session.GlobalSetupTeardownListener;
			}
			""";

	private static List<String> compile(Path temp, Writer out, Writer err) throws Exception {
		var documentation = Files.createDirectories(temp.resolve("src/documentation"));
		Files.writeString(documentation.resolve("module-info.java"), DOCUMENTATION_MODULE_DESCRIPTOR);

		var args = new ArrayList<String>();
		args.add("-Xlint"); // enable all default warnings
		args.add("-proc:none"); // disable annotation processing
		args.add("-cp");
		args.add(""); // set empty class path, otherwise system property "java.class.path" is read

		args.add("-d");
		args.add(temp.resolve("destination").toString());

		var lib = Files.createDirectories(temp.resolve("lib"));
		ThirdPartyJars.copy(lib, "junit", "junit");
		ThirdPartyJars.copy(lib, "org.assertj", "assertj-core");
		// Byte Buddy is used by AssertJ's soft assertions which are used by the Engine Test Kit
		ThirdPartyJars.copy(lib, "net.bytebuddy", "byte-buddy");
		ThirdPartyJars.copy(lib, "org.apiguardian", "apiguardian-api");
		ThirdPartyJars.copy(lib, "org.hamcrest", "hamcrest");
		ThirdPartyJars.copy(lib, "org.opentest4j", "opentest4j");
		ThirdPartyJars.copy(lib, "org.opentest4j.reporting", "open-test-reporting-tooling-spi");
		ThirdPartyJars.copy(lib, "com.google.jimfs", "jimfs");
		ThirdPartyJars.copy(lib, "com.google.guava", "guava");
		Helper.loadAllJUnitModules(lib);
		args.add("--module-path");
		args.add(lib.toString());

		args.add("--patch-module");
		args.add("documentation=" + Path.of("../documentation/src/main/java") + File.pathSeparator
				+ Path.of("../documentation/src/test/java"));

		args.add("--module-source-path");
		args.add(temp.resolve("src").toString());

		args.add(documentation.resolve("module-info.java").toString());
		try (var walk = Files.walk(Path.of("../documentation/src/test/java"))) {
			walk.map(Path::toString) //
					.filter(s -> s.endsWith(".java")) //
					// TypeError: systemProperty.get is not a function ?!?!
					.filter(s -> !s.endsWith("ConditionalTestExecutionDemo.java")) //
					// Don't include command-line tools that "require io.github.classgraph"
					.filter(s -> !s.contains("tools")).forEach(args::add);
		}

		var javac = ToolProvider.findFirst("javac").orElseThrow();
		var code = javac.run(new PrintWriter(out), new PrintWriter(err), args.toArray(String[]::new));

		assertEquals(0, code, err.toString());
		assertTrue(out.toString().isBlank(), out.toString());
		return args;
	}

	private static void junit(Path temp) throws Exception {
		var projectDir = Path.of("../documentation");

		var result = ProcessStarters.java() //
				.workingDir(projectDir) //
				.addArguments("-XX:StartFlightRecording:filename=" + temp.resolve("user-guide.jfr")) //
				.addArguments("--show-version", "--show-module-resolution") //
				.addArguments("--module-path", String.join(File.pathSeparator, //
					temp.resolve("destination").toString(), //
					temp.resolve("lib").toString() //
				)) //
				.addArguments("--add-modules", "documentation") //
				.addArguments("--patch-module",
					"documentation=" + projectDir.resolve("src/test/resources").toAbsolutePath()) //
				.addArguments("--module", "org.junit.platform.console") //
				.addArguments("execute") //
				.addArguments("--scan-modules") //
				.addArguments("--config", "enableHttpServer=true") //
				.addArguments("--config", LauncherConstants.OUTPUT_DIR_PROPERTY_NAME + "=" + temp.resolve("reports")) //
				.addArguments("--fail-if-no-tests") //
				.addArguments("--include-classname", ".*Tests") //
				.addArguments("--include-classname", ".*Demo") //
				.addArguments("--exclude-tag", "exclude") //
				.addArguments("--exclude-tag", "exclude") //
				.startAndWait();

		assertEquals(0, result.exitCode());
	}

	@Test
	void runTestsFromUserGuideWithinModularBoundaries(@TempDir Path temp) throws Exception {
		var out = new StringWriter();
		var err = new StringWriter();

		var args = compile(temp, out, err);
		// args.forEach(System.out::println);

		assertTrue(err.toString().isBlank(), () -> err + "\n\n" + String.join("\n", args));
		var listing = Helper.treeWalk(temp);
		assertLinesMatch(List.of( //
			"destination", //
			">> CLASSES >>", //
			"lib", //
			"lib/apiguardian-api-.+\\.jar", //
			"lib/assertj-core-.+\\.jar", //
			"lib/byte-buddy-.+", //
			"lib/guava-.+\\.jar", //
			"lib/hamcrest-.+\\.jar", //
			"lib/jimfs-.+\\.jar", //
			"lib/junit-.+\\.jar", //
			">> ALL JUNIT 5 JARS >>", //
			"lib/opentest4j-.+\\.jar", //
			"src", //
			"src/documentation", //
			"src/documentation/module-info.java" //
		), listing);
		// System.out.println("______________");
		// listing.forEach(System.out::println);

		junit(temp);
	}

}
