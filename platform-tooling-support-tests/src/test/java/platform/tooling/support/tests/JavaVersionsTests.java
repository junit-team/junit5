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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static platform.tooling.support.process.ProcessStarters.currentJdkHome;
import static platform.tooling.support.tests.Projects.copyToWorkspace;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.process.ProcessStarters;

/**
 * @since 1.4
 */
class JavaVersionsTests {

	@GlobalResource
	LocalMavenRepo localMavenRepo;

	@TempDir
	Path workspace;

	@Test
	void java_8() throws Exception {
		var java8Home = Helper.getJavaHome("8");
		assumeTrue(java8Home.isPresent(), "Java 8 installation directory not found!");
		var actualLines = execute(java8Home.get(), Map.of());

		assertTrue(actualLines.contains("[WARNING] Tests run: 2, Failures: 0, Errors: 0, Skipped: 1"));
	}

	@Test
	void java_default() throws Exception {
		var actualLines = execute(currentJdkHome(), MavenEnvVars.FOR_JDK24_AND_LATER);

		assertTrue(actualLines.contains("[WARNING] Tests run: 2, Failures: 0, Errors: 0, Skipped: 1"));
	}

	List<String> execute(Path javaHome, Map<String, String> environmentVars) throws Exception {
		var result = ProcessStarters.maven() //
				.workingDir(copyToWorkspace(Projects.JAVA_VERSIONS, workspace)) //
				.putEnvironment("JAVA_HOME", javaHome) //
				.putEnvironment(environmentVars) //
				.addArguments(localMavenRepo.toCliArgument(), "-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("--update-snapshots", "--batch-mode", "verify") //
				.startAndWait();

		assertEquals(0, result.exitCode());
		assertEquals("", result.stdErr());
		return result.stdOutLines();
	}

}
