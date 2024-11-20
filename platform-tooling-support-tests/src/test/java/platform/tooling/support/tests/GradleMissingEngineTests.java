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
import static platform.tooling.support.Helper.TOOL_TIMEOUT;

import java.nio.file.Paths;

import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.tool.GradleWrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.platform.reporting.testutil.FileUtils;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class GradleMissingEngineTests {

	@ResourceLock(Projects.GRADLE_MISSING_ENGINE)
	@Test
	void gradle_wrapper() {
		test(new GradleWrapper(Paths.get("..")));
	}

	private void test(Tool gradle) {
		var request = Request.builder() //
				.setProject(Projects.GRADLE_MISSING_ENGINE) //
				.setTool(gradle) //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("build", "--no-daemon", "--stacktrace", "--no-build-cache", "--warning-mode=fail") //
				.putEnvironment("JDK8", Helper.getJavaHome("8").orElseThrow(TestAbortedException::new).toString()) //
				.setTimeout(TOOL_TIMEOUT) //
				.build();

		var result = request.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(1, result.getExitCode());
		assertThat(result.getOutputLines("err")) //
				.contains("FAILURE: Build failed with an exception.");

		var htmlFile = FileUtils.findPath(Request.WORKSPACE.resolve(request.getWorkspace()),
			"glob:**/build/reports/tests/test/classes/*.html");
		assertThat(htmlFile).content() //
				.contains("Cannot create Launcher without at least one TestEngine");
	}
}
