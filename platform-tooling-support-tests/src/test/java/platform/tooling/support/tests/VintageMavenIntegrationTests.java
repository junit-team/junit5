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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static platform.tooling.support.Helper.TOOL_TIMEOUT;

import de.sormuras.bartholdy.Result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

class VintageMavenIntegrationTests {

	@Test
	void unsupportedVersion() {
		var result = run("4.11");

		assertThat(result.getExitCode()).isEqualTo(0);
		assertThat(result.getOutput("out")) //
				.contains("Tests run: 0, Failures: 0, Errors: 0, Skipped: 0");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "4.12", "4.13.2" })
	void supportedVersions(String version) {
		var result = run(version);

		assertThat(result.getExitCode()).isGreaterThan(0);
		assertThat(result.getOutput("out")) //
				.contains("Running com.example.vintage.VintageTest") //
				.contains("VintageTest.failure:") //
				.contains("Tests run: 2, Failures: 1, Errors: 0, Skipped: 0");

		var surefireReportsDir = Request.WORKSPACE.resolve("vintage-maven-" + version).resolve(
			"target/surefire-reports");
		assertThat(surefireReportsDir.resolve("com.example.vintage.VintageTest.txt")).isRegularFile();
		assertThat(surefireReportsDir.resolve("TEST-com.example.vintage.VintageTest.xml")).isRegularFile();
	}

	private Result run(String version) {
		var result = Request.builder() //
				.setTool(Request.maven()) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.setProject("vintage") //
				.setWorkspace("vintage-maven-" + version) //
				.addArguments("clean", "test", "--update-snapshots", "--batch-mode") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Djunit4Version=" + version) //
				.setTimeout(TOOL_TIMEOUT) //
				.build() //
				.run();
		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);
		return result;
	}

}
