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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * {@code UniqueIdTrackingListener} is a {@link TestExecutionListener} that tracks
 * the {@linkplain TestIdentifier#getUniqueId() unique IDs} of all
 * {@linkplain TestIdentifier#isTest() tests} that were executed and generates a
 * file containing the unique IDs, one ID per line, encoding using UTF-8.
 *
 * <p>The file is currently hard coded to {@code ./build/junit-platform-unique-test-ids.txt}
 * with Gradle or {@code ./target/junit-platform-unique-test-ids.txt} with Maven.
 *
 * @since 1.8
 */
@API(status = EXPERIMENTAL, since = "1.8")
public class UniqueIdTrackingListener implements TestExecutionListener {

	/**
	 * TODO Document constant.
	 */
	public static final String LISTENER_ENABLED_PROPERTY_NAME = "junit.platform.listeners.uid.tracking.enabled";

	/**
	 * TODO Document constant.
	 */
	public static final String OUTPUT_DIR_PROPERTY_NAME = "junit.platform.listeners.uid.tracking.output.dir";

	/**
	 * TODO Document constant.
	 */
	public static final String OUTPUT_FILE_PROPERTY_NAME = "junit.platform.listeners.uid.tracking.output.file";

	/**
	 * TODO Document constant.
	 */
	public static final String DEFAULT_FILE_NAME = "junit-platform-unique-test-ids.txt";

	private final Logger logger = LoggerFactory.getLogger(UniqueIdTrackingListener.class);

	private final List<String> uniqueIds = new ArrayList<>();

	private final boolean enabled;

	public UniqueIdTrackingListener() {
		this.enabled = Boolean.getBoolean(LISTENER_ENABLED_PROPERTY_NAME);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (this.enabled && testIdentifier.isTest()) {
			this.uniqueIds.add(testIdentifier.getUniqueId());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (this.enabled) {
			Path outputFile;
			try {
				outputFile = getOutputFile();
			}
			catch (IOException ex) {
				logger.error(ex, () -> "Failed to create output file");
				// Abort since we cannot generate the file.
				return;
			}

			try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {
				logger.debug(() -> "Writing unique IDs to output file " + outputFile.toAbsolutePath());
				this.uniqueIds.forEach(writer::println);
				writer.flush();
			}
			catch (IOException ex) {
				logger.error(ex, () -> "Failed to write unique IDs to output file " + outputFile.toAbsolutePath());
			}
		}
	}

	private Path getOutputFile() throws IOException {
		String filename = System.getProperty(OUTPUT_FILE_PROPERTY_NAME, DEFAULT_FILE_NAME);
		Path outputFile = getOutputDir().resolve(filename);

		if (Files.exists(outputFile)) {
			Files.delete(outputFile);
		}

		Files.createFile(outputFile);

		return outputFile;
	}

	private Path getOutputDir() throws IOException {
		Path outputDir;

		String customDir = System.getProperty(OUTPUT_DIR_PROPERTY_NAME);
		if (StringUtils.isNotBlank(customDir)) {
			outputDir = Paths.get(customDir);
		}
		else if (Files.exists(Paths.get("pom.xml"))) {
			outputDir = Paths.get("target");
		}
		else {
			outputDir = Paths.get("build");
		}
		//		else if (Files.exists(Paths.get("build.gradle")) || Files.exists(Paths.get("build.gradle.kts"))) {
		//			outputDir = new File("build");
		//		}
		//		else {
		//			outputDir = new File(".");
		//		}

		if (!Files.exists(outputDir)) {
			Files.createDirectories(outputDir);
		}

		return outputDir;
	}

}
