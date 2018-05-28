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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Tool;

/**
 * @since 1.3
 */
class AntStarterTests {

	@Test
	void ant_1_10_3() {
		var standalone = Paths.get("..", "junit-platform-console-standalone", "build", "libs");
		var version = "1.10.3";
		var toolPath = Paths.get("build", "test-tools");
		var antHome = toolPath.resolve("apache-ant-" + version).toString();
		var result = Tool.ANT.builder(version) //
				.setProject("ant-starter") //
				.addArguments("-verbose", "-lib", standalone.toAbsolutePath()) //
				// Set ANT_HOME environment variable to prevent local installation from being used
				.putEnvironment("ANT_HOME", antHome) //
				.build() //
				.run();

		assertEquals(0, result.getStatus());
		assertEquals(List.of(), result.getErrorLines(), "error log isn't empty");
		assertLinesMatch(List.of(">> HEAD >>", //
			"test.junit.launcher:", //
			">>>>", //
			"     \\[echo\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"     \\[echo\\] \\[         5 tests successful      \\]", //
			"     \\[echo\\] \\[         0 tests failed          \\]", //
			">>>>", //
			"test.console.launcher:", //
			">>>>", //
			"     \\[java\\] Test run finished after [\\d]+ ms", //
			">>>>", //
			"     \\[java\\] \\[         5 tests successful      \\]", //
			"     \\[java\\] \\[         0 tests failed          \\]", //
			">> TAIL >>"), //
			result.getOutputLines());
	}

}
