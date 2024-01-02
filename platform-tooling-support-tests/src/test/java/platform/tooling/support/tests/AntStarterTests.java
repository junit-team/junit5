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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.util.List;

import de.sormuras.bartholdy.tool.Java;

import org.apache.tools.ant.Main;
import org.junit.jupiter.api.Test;

import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class AntStarterTests {

	@Test
	void ant_starter() {
		var request = Request.builder() //
				.setTool(new Java()) //
				.setProject("ant-starter") //
				.addArguments("-cp", System.getProperty("antJars"), Main.class.getName()) //
				.addArguments("-verbose") //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"), "error log isn't empty");
		assertLinesMatch(List.of(">> HEAD >>", //
			"test.junit.launcher:", //
			">>>>", //
			"\\[junitlauncher\\] Tests run: 5, Failures: 0, Aborted: 0, Skipped: 0, Time elapsed: .+ sec", //
			">>>>", //
			"test.console.launcher:", //
			">>>>", //
			"     \\[java\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"     \\[java\\] \\[         5 tests successful      \\]", //
			"     \\[java\\] \\[         0 tests failed          \\]", //
			">> TAIL >>"), //
			result.getOutputLines("out"));

		var testResultsDir = Request.WORKSPACE.resolve(request.getWorkspace()).resolve("build/test-report");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir);
	}
}
