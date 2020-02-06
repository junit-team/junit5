/*
 * Copyright 2015-2020 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.file.Paths;
import java.util.List;

import de.sormuras.bartholdy.tool.Ant;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class AntStarterTests {

	@Test
	void ant_1_10_6() {
		var standalone = Paths.get("..", "junit-platform-console-standalone", "build", "libs");
		var result = Request.builder() //
				.setTool(Ant.install("1.10.6", Paths.get("build", "test-tools"))) //
				.setProject("ant-starter") //
				.addArguments("-verbose", "-lib", standalone.toAbsolutePath()) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.build() //
				.run();

		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"), "error log isn't empty");
		assertThat(result.getOutput("out")).contains("Using Java version: 1.8");
		assertLinesMatch(List.of(">> HEAD >>", //
			"test.junit.launcher:", //
			">>>>", //
			"\\[junitlauncher\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"\\[junitlauncher\\] \\[         5 tests successful      \\]", //
			"\\[junitlauncher\\] \\[         0 tests failed          \\]", //
			">>>>", //
			"test.console.launcher:", //
			">>>>", //
			"     \\[java\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"     \\[java\\] \\[         5 tests successful      \\]", //
			"     \\[java\\] \\[         0 tests failed          \\]", //
			">> TAIL >>"), //
			result.getOutputLines("out"));
	}

}
