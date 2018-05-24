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

import platform.tooling.support.BuildRequest;
import platform.tooling.support.BuildTool;
import platform.tooling.support.ToolSupport;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@Test
	void maven_3_5_3() throws Exception {
		var project = "maven-starter";
		var maven = new ToolSupport(BuildTool.MAVEN, "3.5.3");
		var executable = maven.init();
		var request = BuildRequest.builder() //
				.setProject(project) //
				.setWorkspace(project) //
				.setExecutable(executable) //
				.addArguments("--debug", "verify") //
				.build();
		var result = maven.run(request);

		assertEquals(0, result.getStatus());
		assertTrue(result.getOutputLines().contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
	}
}
