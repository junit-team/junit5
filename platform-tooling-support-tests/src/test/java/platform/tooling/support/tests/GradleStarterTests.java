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

import java.nio.file.Paths;

import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleStarterTests {

	@Test
	void gradle_wrapper() {
		var request = Request.builder() //
				.setTool(new GradleWrapper(Paths.get(".."))) //
				.setProject("gradle-starter") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("build", "--no-daemon", "--stacktrace") //
				.setTimeout(TOOL_TIMEOUT) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertTrue(result.getOutputLines("out").stream().anyMatch(line -> line.contains("BUILD SUCCESSFUL")));
		assertThat(result.getOutput("out")).contains("Using Java version: 1.8");

		var testResultsDir = Request.WORKSPACE.resolve(request.getWorkspace()).resolve("build/test-results/test");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir);
	}
}
