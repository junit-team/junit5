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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.Helper.TOOL_TIMEOUT;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@Test
	void verifyMavenStarterProject() {
		var request = Request.builder() //
				.setTool(Request.maven()) //
				.setProject("maven-starter") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("--update-snapshots", "--batch-mode", "verify") //
				.setTimeout(TOOL_TIMEOUT) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));
		assertTrue(result.getOutputLines("out").contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
		assertThat(result.getOutput("out")).contains("Using Java version: 1.8");

		var testResultsDir = Request.WORKSPACE.resolve(request.getWorkspace()).resolve("target/surefire-reports");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir);
	}
}
