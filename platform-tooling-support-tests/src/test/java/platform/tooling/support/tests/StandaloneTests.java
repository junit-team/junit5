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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import de.sormuras.bartholdy.jdk.Jar;
import de.sormuras.bartholdy.jdk.Javac;
import de.sormuras.bartholdy.tool.Java;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import platform.tooling.support.Helper;
import platform.tooling.support.Request;

/**
 * @since 1.4
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StandaloneTests {

	@Test
	@Order(1)
	void compile() throws Exception {
		var workspace = Request.WORKSPACE.resolve("standalone");
		var result = Request.builder() //
				.setTool(new Javac()) //
				.setProject("standalone") //
				.addArguments("-d", workspace.resolve("bin")) //
				.addArguments("--class-path", Helper.createJarPath("junit-platform-console-standalone")) //
				.addArguments(workspace.resolve("src/standalone/JupiterIntegration.java")) //
				.addArguments(workspace.resolve("src/standalone/JupiterParamsIntegration.java")) //
				.addArguments(workspace.resolve("src/standalone/VintageIntegration.java")).build() //
				.run();

		assertEquals(0, result.getExitCode(), String.join("\n", result.getOutputLines("out")));
		assertTrue(result.getOutput("out").isEmpty());
		assertTrue(result.getOutput("err").isEmpty());

		// create "tests.jar" that'll be picked-up by "testWithJarredTestClasses()" later
		var jarFolder = Files.createDirectories(workspace.resolve("jar"));
		var jarResult = Request.builder() //
				.setTool(new Jar()) //
				.setProject("standalone") //
				.addArguments("--create") //
				.addArguments("--file", jarFolder.resolve("tests.jar")) //
				.addArguments("-C", workspace.resolve("bin"), ".") //
				.build().run(false);
		assertEquals(0, jarResult.getExitCode(), String.join("\n", jarResult.getOutputLines("out")));
	}

	@Test
	@Order(2)
	void test() throws IOException {
		var root = Path.of("../../..");
		var jar = root.resolve(Helper.createJarPath("junit-platform-console-standalone"));
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("-jar", jar) //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--classpath", "bin").build() //
				.run(false);

		assertEquals(1, result.getExitCode(), String.join("\n", result.getOutputLines("out")));

		var workspace = Request.WORKSPACE.resolve("standalone");
		var expectedOutLines = Files.readAllLines(workspace.resolve("expected-out.txt"));
		var expectedErrLines = Files.readAllLines(workspace.resolve("expected-err.txt"));
		assertLinesMatch(expectedOutLines, result.getOutputLines("out"));
		assertLinesMatch(expectedErrLines, result.getOutputLines("err"));

		var jupiterVersion = Helper.version("junit-jupiter-engine");
		var vintageVersion = Helper.version("junit-vintage-engine");
		assertTrue(result.getOutput("err").contains("junit-jupiter"
				+ " (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: " + jupiterVersion));
		assertTrue(result.getOutput("err").contains("junit-vintage"
				+ " (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: " + vintageVersion));
	}

	@Test
	@Order(3)
	@Disabled("https://github.com/junit-team/junit5/issues/1724")
	void testWithJarredTestClasses() {
		var root = Path.of("../../..");
		var jar = root.resolve(Helper.createJarPath("junit-platform-console-standalone"));
		var path = new ArrayList<String>();
		// path.add("bin"); // "exploded" test classes are found, see also test() above
		path.add(Request.WORKSPACE.resolve("standalone/jar/tests.jar").toAbsolutePath().toString());
		path.add(jar.toString());
		var result = Request.builder() //
				.setTool(new Java()) //
				.setProject("standalone") //
				.addArguments("--show-version") //
				.addArguments("-enableassertions") //
				.addArguments("-Djava.util.logging.config.file=logging.properties") //
				.addArguments("--class-path", String.join(File.pathSeparator, path)) //
				.addArguments("org.junit.platform.console.ConsoleLauncher") //
				.addArguments("--scan-class-path") //
				.addArguments("--disable-banner") //
				.addArguments("--include-classname", "standalone.*") //
				.addArguments("--fail-if-no-tests") //
				.build() //
				.run(false);

		assertEquals(1, result.getExitCode(), String.join("\n", result.getOutputLines("out")));
	}
}
