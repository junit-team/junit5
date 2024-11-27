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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.tests.Projects.copyToWorkspace;
import static platform.tooling.support.tests.XmlAssertions.verifyContainsExpectedStartedOpenTestReport;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.process.ProcessStarters;

/**
 * @since 1.3
 */
class MavenStarterTests {

	@GlobalResource
	LocalMavenRepo localMavenRepo;

	@GlobalResource
	MavenRepoProxy mavenRepoProxy;

	@Test
	void verifyMavenStarterProject(@TempDir Path workspace) throws Exception {
		var result = ProcessStarters.maven() //
				.workingDir(copyToWorkspace(Projects.MAVEN_STARTER, workspace)) //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("-Dsnapshot.repo.url=" + mavenRepoProxy.getBaseUri()) //
				.addArguments("--update-snapshots", "--batch-mode", "verify") //
				.putEnvironment("JAVA_HOME", Helper.getJavaHome("8").orElseThrow(TestAbortedException::new)) //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr());
		assertTrue(result.stdOutLines().contains("[INFO] BUILD SUCCESS"));
		assertTrue(result.stdOutLines().contains("[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0"));
		assertThat(result.stdOut()).contains("Using Java version: 1.8");

		var testResultsDir = workspace.resolve("target/surefire-reports");
		verifyContainsExpectedStartedOpenTestReport(testResultsDir);
	}
}
