/*
 * Copyright 2015-2019 the original author or authors.
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

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@Test
	void verifyMavenStarterProject() {
		var result = Request.builder() //
				.setTool(Request.maven()) //
				.setProject("maven-starter") //
				.addArguments("--debug", "verify") //
				.setTimeout(Duration.ofMinutes(2)) //
				.build() //
				.run();

		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));
		assertTrue(result.getOutputLines("out").contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
	}
}
