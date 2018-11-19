/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import de.sormuras.bartholdy.tool.Java;
import de.sormuras.bartholdy.tool.Maven;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.4
 */
class JavaVersionsTests {

	@Test
	void java_8() {
		// TODO Find better way to a JDK installation: env var, ~/.m2/toolchains.xml,...
		var java8Home = Paths.get("C:\\Dev\\Java\\jdk1.8.0_144");
		assumeTrue(Files.isDirectory(java8Home), "Java 8 home not found: " + java8Home);
		var actualLines = execute("8", java8Home.toString());

		assertTrue(actualLines.contains("[WARNING] Tests run: 2, Failures: 0, Errors: 0, Skipped: 1"));
	}

	@Test
	void java_default() {
		var actualLines = execute("default", new Java().getHome().toString());

		assertTrue(actualLines.contains("[WARNING] Tests run: 2, Failures: 0, Errors: 0, Skipped: 1"));
	}

	List<String> execute(String version, String javaHome) {
		var result = Request.builder() //
				.setTool(Maven.install("3.6.0", Paths.get("build", "test-tools"))) //
				.setProject("java-versions") //
				.setWorkspace("java-versions-" + version) //
				.addArguments("--debug", "verify") //
				.setTimeout(Duration.ofMinutes(2)) //
				.putEnvironment("JAVA_HOME", javaHome) //
				.build().run();
		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);
		assertEquals(0, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		return result.getOutputLines("out");
	}

}
