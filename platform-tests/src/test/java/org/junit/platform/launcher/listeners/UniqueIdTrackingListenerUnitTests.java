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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UniqueIdTrackingListener}.
 *
 * @since 1.8
 */
class UniqueIdTrackingListenerUnitTests {

	@Test
	void getOutputDirUsesCustomOutputDir() throws Exception {
		try {
			String customDir = "build/UniqueIdTrackingListenerIntegrationTests";
			System.setProperty(OUTPUT_DIR_PROPERTY_NAME, customDir);

			UniqueIdTrackingListener listener = new UniqueIdTrackingListener();
			Path outputDir = listener.getOutputDir();
			assertThat(Files.isSameFile(Paths.get(customDir), outputDir)).isTrue();
			assertThat(outputDir).exists();
		}
		finally {
			System.clearProperty(OUTPUT_DIR_PROPERTY_NAME);
		}
	}

	@Test
	void getOutputDirFallsBackToCurrentWorkingDir() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking";

		UniqueIdTrackingListener listener = new UniqueIdTrackingListener() {
			@Override
			Path currentWorkingDir() {
				return Paths.get(cwd);
			}
		};
		Path outputDir = listener.getOutputDir();
		assertThat(Files.isSameFile(Paths.get(cwd), outputDir)).isTrue();
		assertThat(outputDir).exists();
	}

	@Test
	void getOutputDirDetectsMavenPom() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/maven";
		String expected = "target";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleGroovyDefaultBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/groovy";
		String expected = "build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleGroovyCustomBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/groovy/sub-project";
		String expected = "build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleKotlinDefaultBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/kotlin";
		String expected = "build";

		assertOutputDirIsDetected(cwd, expected);
	}

	@Test
	void getOutputDirDetectsGradleKotlinCustomBuildScript() throws Exception {
		String cwd = "src/test/resources/listeners/uidtracking/gradle/kotlin/sub-project";
		String expected = "build";

		assertOutputDirIsDetected(cwd, expected);
	}

	private void assertOutputDirIsDetected(String cwd, String expected) throws IOException {
		UniqueIdTrackingListener listener = new UniqueIdTrackingListener() {
			@Override
			Path currentWorkingDir() {
				return Paths.get(cwd);
			}
		};
		Path outputDir = listener.getOutputDir();
		assertThat(Files.isSameFile(Paths.get(cwd, expected), outputDir)).isTrue();
		assertThat(outputDir).exists();
	}

}
