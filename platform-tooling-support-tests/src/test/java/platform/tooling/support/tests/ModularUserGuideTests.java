/*
 * Copyright 2015-2020 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.spi.ToolProvider;

import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import platform.tooling.support.Helper;

/**
 * @since 1.5
 */
class ModularUserGuideTests {

	private static final List<String> DOCUMENTATION_MODULE_DESCRIPTOR = List.of( //
		"open module documentation {", //
		"  exports example.testkit;", // just here to ensure documentation example sources are compiled
		//
		"  requires org.junit.jupiter.api;", //
		"  requires org.junit.jupiter.migrationsupport;", //
		"  requires org.junit.jupiter.params;", //
		//
		"  requires org.junit.platform.engine;", //
		"  requires org.junit.platform.reporting;", //
		"  requires org.junit.platform.runner;", //
		"  requires org.junit.platform.testkit;", //
		//
		"  requires java.desktop;", //
		"  requires java.logging;", //
		"  requires java.scripting;", //
		"}", //
		"" //
	);

	private static List<String> compile(Path temp, Writer out, Writer err) throws Exception {
		var documentation = Files.createDirectories(temp.resolve("src/documentation"));
		Files.write(documentation.resolve("module-info.java"), DOCUMENTATION_MODULE_DESCRIPTOR);

		var args = new ArrayList<String>();
		args.add("-Xlint"); // enable all default warnings
		args.add("-proc:none"); // disable annotation processing
		args.add("-cp");
		args.add(""); // set empty class path, otherwise system property "java.class.path" is read

		args.add("-d");
		args.add(temp.resolve("destination").toString());

		var lib = Files.createDirectories(temp.resolve("lib"));
		Helper.load(lib, "junit", "junit", Helper.version("junit4", "4.13"));
		Helper.load(lib, "org.assertj", "assertj-core", Helper.version("assertJ", "3.14.0"));
		Helper.load(lib, "org.apiguardian", "apiguardian-api", Helper.version("apiGuardian", "1.1.0"));
		Helper.load(lib, "org.hamcrest", "hamcrest", Helper.version("hamcrest", "2.2"));
		Helper.load(lib, "org.opentest4j", "opentest4j", Helper.version("ota4j", "1.2.0"));
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

	private static List<String> junit(Path temp, Writer out, Writer err) throws Exception {
		var command = new ArrayList<String>();
		var projectDir = Path.of("../documentation");
		command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());

		command.add("--show-version");
		command.add("--show-module-resolution");

		command.add("--module-path");
		command.add(String.join(File.pathSeparator, //
			temp.resolve("destination").toString(), //
			temp.resolve("lib").toString() //
		));

		command.add("--add-modules");
		command.add("documentation");

		// TODO This `patch-module` should work! Why doesn't it?
		// command.add("--patch-module");
		// command.add("documentation=../documentation/src/test/resources/");
		Files.copy(projectDir.resolve("src/test/resources/two-column.csv"),
			temp.resolve("destination/documentation/two-column.csv"));

		command.add("--module");
		command.add("org.junit.platform.console");

		command.add("--scan-modules");

		command.add("--fail-if-no-tests");
		command.add("--include-classname");
		command.add(".*Tests");
		command.add("--include-classname");
		command.add(".*Demo");
		command.add("--exclude-tag");
		command.add("exclude");

		// System.out.println("______________");
		// command.forEach(System.out::println);

		var builder = new ProcessBuilder(command).directory(projectDir.toFile());
		var java = builder.start();
		ProcessGroovyMethods.waitForProcessOutput(java, out, err);
		var code = java.exitValue();

		if (code != 0) {
			System.out.println(out);
			System.err.println(err);
			fail("Unexpected exit code: " + code);
		}
		return command;
	}

	@Test
	void runTestsFromUserGuideWithinModularBoundaries(@TempDir Path temp) throws Exception {
		var out = new StringWriter();
		var err = new StringWriter();

		var args = compile(temp, out, err);
		// args.forEach(System.out::println);

		assertTrue(err.toString().isBlank(), () -> err.toString() + "\n\n" + String.join("\n", args));
		var listing = Helper.treeWalk(temp);
		assertLinesMatch(List.of( //
			"destination", //
			">> CLASSES >>", //
			"lib", //
			"lib/apiguardian-api-.+\\.jar", //
			"lib/assertj-core-.+\\.jar", //
			"lib/hamcrest-.+\\.jar", //
			"lib/junit-.+\\.jar", //
			">> ALL JUNIT 5 JARS >>", //
			"lib/opentest4j-.+\\.jar", //
			"src", //
			"src/documentation", //
			"src/documentation/module-info.java" //
		), listing);
		// System.out.println("______________");
		// listing.forEach(System.out::println);

		junit(temp, out, err);
	}
}
