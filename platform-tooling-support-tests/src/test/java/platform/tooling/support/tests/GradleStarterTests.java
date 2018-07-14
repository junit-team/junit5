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

import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleStarterTests {

	@Test
	void gradle_wrapper() {
		var result = Request.builder() //
				.setTool(new GradleWrapper(Paths.get(".."))) //
				.setProject("gradle-starter") //
				.addArguments("build", "--no-daemon", "--debug", "--stacktrace") //
				.build() //
				.run();

		assertEquals(0, result.getExitCode());
		assertTrue(result.getOutputLines("out").stream().anyMatch(line -> line.contains("BUILD SUCCESSFUL")));
	}
}
