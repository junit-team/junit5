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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;
import java.util.List;

import org.apache.tools.ant.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import platform.tooling.support.process.ProcessStarter;

/**
 * @since 1.3
 */
class AntStarterTests {

	@ResourceLock(Projects.ANT_STARTER)
	@Test
	@Timeout(60)
	void ant_starter(@TempDir Path workspace) throws Exception {
		copyToWorkspace(Projects.ANT_STARTER, workspace);

		var result = ProcessStarter.java() //
				.workingDir(workspace) //
				.addArguments("-cp", System.getProperty("antJars"), Main.class.getName()) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr(), "error log isn't empty");
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
			result.stdOutLines());

		var testResultsDir = workspace.resolve("build/test-report");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir);
	}

}
