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
import static platform.tooling.support.Helper.TOOL_TIMEOUT;

import java.nio.file.Paths;
import java.util.List;

import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleMissingEngineTests {

	@Test
	void gradle_wrapper() {
		test(new GradleWrapper(Paths.get("..")));
	}

	private void test(Tool gradle) {
		var project = "gradle-missing-engine";
		var result = Request.builder() //
				.setProject(project) //
				.setWorkspace(project + "-wrapper") //
				.setTool(gradle) //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("build", "--no-daemon", "--debug", "--stacktrace") //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.setTimeout(TOOL_TIMEOUT).build() //
				.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(1, result.getExitCode());
		assertLinesMatch(List.of( //
			">> HEAD >>", //
			".+DEBUG.+Cannot create Launcher without at least one TestEngine.+", //
			">> TAIL >>"), //
			result.getOutputLines("out"));
		assertLinesMatch(List.of( //
			">> HEAD >>", //
			".+ERROR.+FAILURE: Build failed with an exception.", //
			">> TAIL >>"), //
			result.getOutputLines("err"));
	}
}
