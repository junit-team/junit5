/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static java.util.Collections.singletonList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
// tag::user_guide[]
class TestReporterDemo {

	@Test
	void reportSingleValue(TestReporter testReporter) {
		testReporter.publishEntry("a status message");
	}

	@Test
	void reportKeyValuePair(TestReporter testReporter) {
		testReporter.publishEntry("a key", "a value");
	}

	@Test
	void reportMultipleKeyValuePairs(TestReporter testReporter) {
		Map<String, String> values = new HashMap<>();
		values.put("user name", "dk38");
		values.put("award year", "1974");

		testReporter.publishEntry(values);
	}

	@Test
	void reportFiles(TestReporter testReporter, @TempDir Path tempDir) throws Exception {

		testReporter.publishFile("test1.txt", file -> Files.write(file, singletonList("Test 1")));

		Path existingFile = Files.write(tempDir.resolve("test2.txt"), singletonList("Test 2"));
		testReporter.publishFile(existingFile);

		testReporter.publishFile("test3", dir -> {
			Path nestedFile = Files.createDirectory(dir).resolve("nested.txt");
			Files.write(nestedFile, singletonList("Nested content"));
		});
	}
}
// end::user_guide[]
