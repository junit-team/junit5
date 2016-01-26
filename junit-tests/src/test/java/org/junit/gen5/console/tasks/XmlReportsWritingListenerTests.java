/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.Launcher;

class XmlReportsWritingListenerTests {

	private Path tempDirectory;

	@BeforeEach
	void createTempDirectory(TestInfo testInfo) throws Exception {
		tempDirectory = Files.createTempDirectory(testInfo.getName());
	}

	@AfterEach
	void deleteTempDirectory() throws Exception {
		Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return deleteAndContinue(file);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return deleteAndContinue(dir);
			}

			private FileVisitResult deleteAndContinue(Path path) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Test
	void writesFileForSingleSucceedingTest() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("succeedingTest", () -> {
		});

		executeTests(engine);

		String content = readFile("TEST-dummy.xml");
		//@formatter:off
		assertThat(content)
			.contains("<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"0\"")
			.contains("<testcase name=\"succeedingTest\"")
			.doesNotContain("<skipped")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:off
	}

	private void executeTests(DummyTestEngine engine) {
		PrintWriter out = new PrintWriter(new StringWriter());
		XmlReportsWritingListener reportListener = new XmlReportsWritingListener(tempDirectory.toString(), out);
		Launcher launcher = createLauncher(engine);
		launcher.registerTestExecutionListeners(reportListener);
		launcher.execute(request().select(forUniqueId(engine.getId())).build());
	}

	private String readFile(String filename) throws IOException {
		Path xmlFile = tempDirectory.resolve(filename);
		assertTrue(Files.exists(xmlFile), () -> "File does not exist: " + xmlFile);
		return new String(Files.readAllBytes(xmlFile), UTF_8);
	}
}
