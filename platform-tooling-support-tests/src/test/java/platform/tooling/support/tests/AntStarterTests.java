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
import platform.tooling.support.ToolRequest;
import platform.tooling.support.ToolSupport;

class AntStarterTests {

	@Test
	void ant_1_10_3() throws Exception {
		var project = "ant-starter";
		var ant = new ToolSupport(Tool.ANT, "1.10.3");
		var executable = ant.init();
		var standalone = Paths.get("..", "junit-platform-console-standalone", "build", "libs");
		var request = ToolRequest.builder() //
				.setProject(project) //
				.setWorkspace(project) //
				.setExecutable(executable) //
				.addArguments("-verbose", "-lib", standalone.toAbsolutePath()) //
				.build();
		var response = ant.run(request);

		assertEquals(0, response.getStatus());
		assertEquals(List.of(), response.getErrorLines(), "error log isn't empty");
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
			response.getOutputLines());
	}
}
