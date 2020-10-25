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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.time.Duration;

import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleKotlinExtensionsTests {

	@Test
	void gradle_wrapper() {
		var result = Request.builder() //
				.setTool(new GradleWrapper(Request.PROJECTS.resolve("gradle-kotlin-extensions"))) //
				.setProject("gradle-kotlin-extensions") //
				.addArguments("-Dmaven.repo=" + System.getProperty("maven.repo")) //
				.addArguments("build", "--no-daemon", "--debug", "--stacktrace") //
				.setTimeout(Duration.ofMinutes(2)) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.build() //
				.run();

		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode(), "result=" + result);
		assertTrue(result.getOutputLines("out").stream().anyMatch(line -> line.contains("BUILD SUCCESSFUL")));
	}
}
