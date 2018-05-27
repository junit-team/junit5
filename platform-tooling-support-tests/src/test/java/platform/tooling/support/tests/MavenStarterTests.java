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

import org.junit.jupiter.api.Test;
import platform.tooling.support.Tool;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@Test
	void maven_3_5_3() {
		var result = Tool.MAVEN.builder("3.5.3") //
				.setProject("maven-starter") //
				.addArguments("--debug", "verify") //
				.build() //
				.run();

		assertEquals(0, result.getStatus());
		assertTrue(result.getOutputLines().contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
	}
}
