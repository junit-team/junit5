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

import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.tool.Gradle;
import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleMissingEngineTests {

	@Test
	void gradle_wrapper() {
		test(new GradleWrapper(Paths.get("..")), "wrapper");
	}

	@Test
	@EnabledOnJre(JRE.JAVA_10)
	void gradle_4_7() {
		test(Gradle.install("4.7", Paths.get("build", "test-tools")), "4.7");
	}

	private void test(Tool gradle, String version) {
		var project = "gradle-missing-engine";
		var result = Request.builder() //
				.setProject(project) //
				.setWorkspace(project + '-' + version) //
				.setTool(gradle) //
				.addArguments("build", "--no-daemon", "--debug", "--stacktrace") //
				.build() //
				.run();

		assertEquals(1, result.getExitCode(), result.toString());
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
