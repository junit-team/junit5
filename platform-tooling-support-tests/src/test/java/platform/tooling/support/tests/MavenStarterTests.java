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

import java.nio.file.Paths;
import java.time.Duration;

import de.sormuras.bartholdy.tool.Maven;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@Test
	void maven_3_5_4() {
		var result = Request.builder() //
				.setTool(Maven.install("3.5.4", Paths.get("build", "test-tools"))) //
				.setProject("maven-starter") //
				.addArguments("--debug", "verify") //
				.setTimeout(Duration.ofSeconds(99)) //
				.build() //
				.run();

		assertEquals(0, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));
		assertTrue(result.getOutputLines("out").contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
	}
}
