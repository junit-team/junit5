/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class OutputDirTests {

	@Test
	void getOutputDirUsesCustomOutputDir() throws Exception {
		String customDir = "build/UniqueIdTrackingListenerIntegrationTests";
		Path outputDir = OutputDir.create(Optional.of(customDir)).toPath();
		assertThat(Files.isSameFile(Paths.get(customDir), outputDir)).isTrue();
		assertThat(outputDir).exists();
	}

	@Test
	void getOutputDirFallsBackToCurrentWorkingDir() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking";
		String expected = cwd;

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsMavenPom() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/maven";
		String expected = cwd + "/target";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleGroovyDefaultBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/groovy";
		String expected = cwd + "/build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleGroovyCustomBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/groovy/sub-project";
		String expected = cwd + "/build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleKotlinDefaultBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/kotlin";
		String expected = cwd + "/build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleKotlinCustomBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/kotlin/sub-project";
		String expected = cwd + "/build";

		assertOutputDirIsDetected(cwd, expected);
	}

	private void assertOutputDirIsDetected(String cwd, String expected) throws IOException {
		Path outputDir = OutputDir.createSafely(Optional.empty(), () -> Paths.get(cwd)).toPath();
		assertThat(Files.isSameFile(Paths.get(expected), outputDir)).isTrue();
		assertThat(outputDir).exists();
	}

}
