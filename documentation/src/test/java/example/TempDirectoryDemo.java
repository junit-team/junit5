/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import example.util.ListWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TempDirectoryDemo {

	// tag::user_guide_parameter_injection[]
	@Test
	void writeItemsToFile(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");

		new ListWriter(file).write("a", "b", "c");

		assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
	}
	// end::user_guide_parameter_injection[]

	// tag::user_guide_multiple_directories[]
	@Test
	void copyFileFromSourceToTarget(@TempDir Path source, @TempDir Path target) throws IOException {
		Path sourceFile = source.resolve("test.txt");
		new ListWriter(sourceFile).write("a", "b", "c");

		Path targetFile = Files.copy(sourceFile, target.resolve("test.txt"));

		assertNotEquals(sourceFile, targetFile);
		assertEquals(singletonList("a,b,c"), Files.readAllLines(targetFile));
	}
	// end::user_guide_multiple_directories[]

	static
	// tag::user_guide_field_injection[]
	class SharedTempDirectoryDemo {

		@TempDir
		static Path sharedTempDir;

		@Test
		void writeItemsToFile() throws IOException {
			Path file = sharedTempDir.resolve("test.txt");

			new ListWriter(file).write("a", "b", "c");

			assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
		}

		@Test
		void anotherTestThatUsesTheSameTempDir() {
			// use sharedTempDir
		}

	}
	// end::user_guide_field_injection[]

}
