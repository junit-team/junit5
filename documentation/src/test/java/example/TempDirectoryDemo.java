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
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import example.util.ListWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ResourceSupplier.New;
import org.junit.jupiter.api.extension.ResourceSupplier.Singleton;
import org.junit.jupiter.api.support.io.Temporary;

class TempDirectoryDemo {

	// tag::user_guide_parameter_injection[]
	@Test
	void writeItemsToFile(@Temporary.Directory Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");

		new ListWriter(file).write("a", "b", "c");

		assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
	}
	// end::user_guide_parameter_injection[]

	static class GloballySharedSingletonTempDirectoryDemo {
		@Test
		void text1(@Singleton(Temporary.class) Path temp) throws IOException {
			Path file = temp.resolve("test1.txt");

			new ListWriter(file).write("a", "b", "c");

			assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
		}

		@Test
		void text2(@Singleton(Temporary.class) Path temp) {
			// use same temporary directory
		}
	}

	// tag::user_guide_field_injection[]
	@TestInstance(PER_CLASS)
	static class SharedTempDirectoryDemo {

		private final Path sharedTempDir;

		SharedTempDirectoryDemo(@New(Temporary.class) Path sharedTempDir) {
			this.sharedTempDir = sharedTempDir;
		}

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

		@Test
		void text3(@Singleton(Temporary.class) Path temp) {
			// use same temporary directory as above
		}

	}
	// end::user_guide_field_injection[]

}
