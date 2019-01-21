/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import example.util.ListWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.support.io.TempDirectory;
import org.junit.jupiter.api.support.io.TempDirectory.TempDir;

class TempDirectoryDemo {

	// tag::user_guide_parameter_injection[]
	@Test
	@ExtendWith(TempDirectory.class)
	void writeItemsToFile(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");

		new ListWriter(file).write("a", "b", "c");

		assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
	}
	// end::user_guide_parameter_injection[]

	// tag::user_guide_field_injection[]
	@ExtendWith(TempDirectory.class)
	// end::user_guide_field_injection[]
	static
	// tag::user_guide_field_injection[]
	class FieldInjection {

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
