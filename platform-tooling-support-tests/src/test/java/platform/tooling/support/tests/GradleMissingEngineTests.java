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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import platform.tooling.support.Tool;
import platform.tooling.support.ToolRequest;
import platform.tooling.support.ToolSupport;

class GradleMissingEngineTests {

	@Test
	void gradle_wrapper() throws Exception {
		test(new ToolSupport(Tool.GRADLE), "wrapper");
	}

	@Test
	@EnabledOnJre(JRE.JAVA_10)
	void gradle_4_7() throws Exception {
		test(new ToolSupport(Tool.GRADLE, "4.7"), "4.7");
	}

	private void test(ToolSupport gradle, String workspaceSuffix) throws Exception {
		var project = "gradle-missing-engine";
		var executable = gradle.init();
		var request = ToolRequest.builder() //
				.setProject(project) //
				.setWorkspace(project + '-' + workspaceSuffix) //
				.setExecutable(executable) //
				.addArguments("build", "--no-daemon", "--debug", "--stacktrace") //
				.build();
		var response = gradle.run(request);

		assertEquals(1, response.getStatus());
		assertLinesMatch(List.of( //
			">> HEAD >>", //
			".+DEBUG.+Cannot create Launcher without at least one TestEngine.+", //
			">> TAIL >>"), //
			response.getOutputLines());
		assertLinesMatch(List.of( //
			">> HEAD >>", //
			".+ERROR.+FAILURE: Build failed with an exception.", //
			">> TAIL >>"), //
			response.getErrorLines());
	}
}
