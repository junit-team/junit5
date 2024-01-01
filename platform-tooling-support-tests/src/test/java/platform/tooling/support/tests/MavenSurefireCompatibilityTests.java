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

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.9.2
 */
class MavenSurefireCompatibilityTests {

	@ParameterizedTest
	@CsvSource(delimiter = '|', nullValues = "<none>", textBlock = """
			2.22.2   | --activate-profiles=manual-platform-dependency
			3.0.0-M4 | <none>
			""")
	void testMavenSurefireCompatibilityProject(String surefireVersion, String extraArg) throws IOException {
		var extraArgs = extraArg == null ? new Object[0] : new Object[] { extraArg };
		var request = Request.builder() //
				.setTool(Request.maven()) //
				.setProject("maven-surefire-compatibility") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Dsurefire.version=" + surefireVersion) //
				.addArguments("--update-snapshots", "--batch-mode", "test") //
				.addArguments(extraArgs) //
				.setTimeout(TOOL_TIMEOUT) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"));

		var output = result.getOutputLines("out");
		assertTrue(output.contains("[INFO] BUILD SUCCESS"));
		assertTrue(output.contains("[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));

		var targetDir = Request.WORKSPACE.resolve(request.getWorkspace()).resolve("target");
		try (var stream = Files.list(targetDir)) {
			assertThat(stream.filter(file -> file.getFileName().toString().startsWith("junit-platform-unique-ids"))) //
					.hasSize(1);
		}
	}
}
