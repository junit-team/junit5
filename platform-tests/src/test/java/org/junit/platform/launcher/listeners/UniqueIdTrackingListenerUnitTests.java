/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.launcher.listeners.UniqueIdTrackingListener.OUTPUT_DIR_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Unit tests for the {@link UniqueIdTrackingListener}.
 *
 * @since 1.8
 */
class UniqueIdTrackingListenerUnitTests {

	private final ConfigurationParameters configParams = mock(ConfigurationParameters.class);

	@Test
	void getOutputDirUsesCustomOutputDir() throws Exception {
		String customDir = "build/UniqueIdTrackingListenerIntegrationTests";

		when(configParams.get(OUTPUT_DIR_PROPERTY_NAME)).thenReturn(Optional.of(customDir));

		Path outputDir = new UniqueIdTrackingListener().getOutputDir(configParams);
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
		when(configParams.get(OUTPUT_DIR_PROPERTY_NAME)).thenReturn(Optional.empty());

		UniqueIdTrackingListener listener = new UniqueIdTrackingListener() {
			@Override
			Path currentWorkingDir() {
				return Paths.get(cwd);
			}
		};
		Path outputDir = listener.getOutputDir(configParams);
		assertThat(Files.isSameFile(Paths.get(expected), outputDir)).isTrue();
		assertThat(outputDir).exists();
	}

}
