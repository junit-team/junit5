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
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * {@code UniqueIdTrackingListener} is a {@link TestExecutionListener} that
 * tracks the unique IDs of all tests that were executed and generates a file
 * containing the unique IDs.
 *
 * <p>The file is currently hard coded to {@code ./build/junit-platform-unique-ids.txt}
 * with Gradle or {@code ./target/junit-platform-unique-ids.txt} with Maven.
 *
 * @since 1.8
 */
@API(status = EXPERIMENTAL, since = "1.8")
public class UniqueIdTrackingListener implements TestExecutionListener {

	public static final String DEFAULT_FILE_NAME = "junit-platform-unique-ids.txt";

	private final List<String> uniqueIds = new ArrayList<>();

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			this.uniqueIds.add(testIdentifier.getUniqueId());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		Path outputFile;
		try {
			outputFile = getOutputFile();
		}
		catch (IOException ex) {
			System.err.println("Failed to create output file");
			ex.printStackTrace(System.err);
			// TODO Throw exception or log error.
			return;
		}

		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8), true)) {
			this.uniqueIds.stream().sorted().forEach(writer::println);
		}
		catch (IOException ex) {
			System.err.println("Failed to write unique IDs to output file " + outputFile.toAbsolutePath());
			ex.printStackTrace(System.err);
			// TODO Throw exception or log error.
		}
	}

	private Path getOutputFile() throws IOException {
		Path outputFile = getOutputDir().resolve(DEFAULT_FILE_NAME);

		if (Files.exists(outputFile)) {
			Files.delete(outputFile);
		}

		Files.createFile(outputFile);

		return outputFile;
	}

	private Path getOutputDir() throws IOException {
		Path outputDir;

		if (Files.exists(Paths.get("pom.xml"))) {
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
