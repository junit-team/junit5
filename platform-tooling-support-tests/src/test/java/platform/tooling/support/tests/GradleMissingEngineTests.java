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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import platform.tooling.support.Tool;
import platform.tooling.support.ToolRequest;
import platform.tooling.support.ToolSupport;

@DisplayName(GradleMissingEngineTests.PROJECT)
class GradleMissingEngineTests {

	static final String PROJECT = "gradle-missing-engine";

	static ToolRequest.Builder createToolRequestBuilder(String workspaceSuffix) {
		return ToolRequest.builder() //
				.setProject(PROJECT) //
				.setWorkspace(PROJECT + '-' + workspaceSuffix).addArguments("build", "--no-daemon", "--debug",
					"--stacktrace");
	}

	@Test
	@DisplayName("gradle-wrapper")
	void gradle_wrapper() throws Exception {
		var gradle = new ToolSupport(Tool.GRADLE);
		var executable = gradle.init();

		var request = createToolRequestBuilder("wrapper").setExecutable(executable).build();
		var response = gradle.run(request);

		// assert
		Assertions.assertNotEquals(0, response.getStatus());
		Assertions.assertLinesMatch(List.of(">> HEAD >>",
			".+DEBUG.+org.junit.platform.commons.util.PreconditionViolationException: Cannot create Launcher without at least one TestEngine; consider adding an engine implementation JAR to the classpath",
			">> TAIL >>"), response.getOutputLines());
		Assertions.assertLinesMatch(
			List.of(">> HEAD >>", ".+ERROR.+FAILURE: Build failed with an exception.", ">> TAIL >>"),
			response.getErrorLines());
	}

	@Test
	@DisplayName("gradle-4.7")
	void gradle_4_7() throws Exception {
		var gradle = new ToolSupport(Tool.GRADLE, "4.7");
		var executable = gradle.init();

		var request = createToolRequestBuilder("4.7").setExecutable(executable).build();
		var response = gradle.run(request);

		// assert
		Assertions.assertNotEquals(0, response.getStatus());
		Assertions.assertLinesMatch(List.of(">> HEAD >>",
			".+DEBUG.+org.junit.platform.commons.util.PreconditionViolationException: Cannot create Launcher without at least one TestEngine; consider adding an engine implementation JAR to the classpath",
			">> TAIL >>"), response.getOutputLines());
		Assertions.assertLinesMatch(
			List.of(">> HEAD >>", ".+ERROR.+FAILURE: Build failed with an exception.", ">> TAIL >>"),
			response.getErrorLines());
	}
}
