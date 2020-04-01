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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.file.Path;
import java.time.Duration;

import de.sormuras.bartholdy.Result;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.Request;

class VintageMavenIntegrationTests {

	@Test
	void unsupportedVersion() {
		Result result = run("4.11");

		assertThat(result.getExitCode()).isEqualTo(0);
		assertThat(result.getOutput("out")) //
				.contains("Tests run: 0, Failures: 0, Errors: 0, Skipped: 0");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "4.12", "4.13" })
	void supportedVersions(String version) {
		Result result = run(version);

		assertThat(result.getExitCode()).isGreaterThan(0);
		assertThat(result.getOutput("out")) //
				.contains("Running com.example.vintage.VintageTest") //
				.contains("VintageTest.failure:") //
				.contains("Tests run: 2, Failures: 1, Errors: 0, Skipped: 0");

		Path surefireReportsDir = Request.WORKSPACE.resolve("vintage-maven-" + version).resolve(
			"target/surefire-reports");
		assertThat(surefireReportsDir.resolve("com.example.vintage.VintageTest.txt")).isRegularFile();
		assertThat(surefireReportsDir.resolve("TEST-com.example.vintage.VintageTest.xml")).isRegularFile();
	}

	private Result run(String version) {
		Result result = Request.builder() //
				.setTool(Request.maven()) //
				.setJavaHome(Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.setProject("vintage") //
				.setWorkspace("vintage-maven-" + version) //
				.addArguments("clean", "test", "--debug") //
				.addArguments("-Dmaven.repo=" + System.getProperty("maven.repo")) //
				.addArguments("-Djunit4Version=" + version) //
				.setTimeout(Duration.ofMinutes(2)) //
				.build() //
				.run();
		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);
		return result;
	}

}
