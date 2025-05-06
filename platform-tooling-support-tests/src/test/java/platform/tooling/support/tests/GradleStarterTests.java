/*
 * Copyright 2015-2025 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;

import de.skuzzle.test.snapshots.Snapshot;
import de.skuzzle.test.snapshots.junit5.EnableSnapshotTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.tests.process.OutputFiles;
import org.junit.platform.tests.process.ProcessResult;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.ProcessStarters;

/**
 * @since 1.3
 */
@EnableSnapshotTests
//@SnapshotTestOptions(alwaysPersistActualResult = true)
class GradleStarterTests {

	@TempDir
	Path workspace;

	@BeforeEach
	void prepareWorkspace() throws Exception {
		copyToWorkspace(Projects.JUPITER_STARTER, workspace);
	}

	@ParameterizedTest(name = "Java {0}")
	@ValueSource(ints = { 8, 17 })
	void buildJupiterStarterProject(int javaVersion, @FilePrefix("gradle") OutputFiles outputFiles, Snapshot snapshot)
			throws Exception {

		var result = runGradle(outputFiles, javaVersion, "build");

		assertThat(result.stdOut()) //
				.contains( //
					"CalculatorParameterizedClassTests > [1] i=1 > parameterizedTest(int)", //
					"CalculatorParameterizedClassTests > [1] i=1 > Inner > [1] 1 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [1] i=1 > Inner > [2] 2 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [2] i=2 > parameterizedTest(int)", //
					"CalculatorParameterizedClassTests > [2] i=2 > Inner > [1] 1 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [2] i=2 > Inner > [2] 2 > regularTest() PASSED", //
					"Using Java version: " + (javaVersion == 8 ? "1.8" : javaVersion), //
					"CalculatorTests > 1 + 1 = 2 PASSED", //
					"CalculatorTests > add(int, int, int) > 0 + 1 = 1 PASSED", //
					"CalculatorTests > add(int, int, int) > 1 + 2 = 3 PASSED", //
					"CalculatorTests > add(int, int, int) > 49 + 51 = 100 PASSED", //
					"CalculatorTests > add(int, int, int) > 1 + 100 = 101 PASSED" //
				);

		var testResultsDir = workspace.resolve("build/test-results/test");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir, snapshot);
	}

	@Test
	void runOnlyOneMethodInClassTemplate(@FilePrefix("gradle") OutputFiles outputFiles) throws Exception {

		var result = runGradle(outputFiles, 8, "test", "--tests", "CalculatorParameterized*.regular*");

		assertThat(result.stdOut()) //
				.contains( //
					"CalculatorParameterizedClassTests > [1] i=1 > Inner > [1] 1 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [1] i=1 > Inner > [2] 2 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [2] i=2 > Inner > [1] 1 > regularTest() PASSED", //
					"CalculatorParameterizedClassTests > [2] i=2 > Inner > [2] 2 > regularTest() PASSED" //
				) //
				.doesNotContain("parameterizedTest(int)", "CalculatorTests");

		result = runGradle(outputFiles, 8, "test", "--tests", "*ParameterizedClassTests.parameterized*");

		assertThat(result.stdOut()) //
				.contains( //
					"CalculatorParameterizedClassTests > [1] i=1 > parameterizedTest(int)", //
					"CalculatorParameterizedClassTests > [2] i=2 > parameterizedTest(int)" //
				) //
				.doesNotContain("regularTest()", "CalculatorTests");
	}

	private ProcessResult runGradle(OutputFiles outputFiles, int javaVersion, String... extraArgs)
			throws InterruptedException {
		var result = ProcessStarters.gradlew() //
				.workingDir(workspace) //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Djava.toolchain.version=" + javaVersion) //
				.addArguments("--stacktrace", "--no-build-cache", "--warning-mode=fail") //
				.addArguments(extraArgs) //
				.putEnvironment("JDK8", Helper.getJavaHome(8).orElseThrow(TestAbortedException::new).toString()) //
				.putEnvironment("JDK17", Helper.getJavaHome(17).orElseThrow(TestAbortedException::new).toString()) //
				.redirectOutput(outputFiles) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertTrue(result.stdOut().lines().anyMatch(line -> line.contains("BUILD SUCCESSFUL")));
		return result;
	}
}
