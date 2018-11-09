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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import example.util.ListWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ResourceSupplier;
import org.junit.jupiter.api.extension.ResourceSupplier.New;

//@formatter:off
// tag::user_guide[]
class TempDirectoryWithCustomFileSystemDemo {

	static class Jim implements ResourceSupplier<Path> {

		private final FileSystem jim;
		private final Path path;

		public Jim() {
			this.jim = Jimfs.newFileSystem(Configuration.unix());
			this.path = jim.getPath("/");
		}

		@Override
		public Path get() {
			return path;
		}

		@Override
		public void close() throws IOException {
			jim.close();
		}
	}

	@Test
	void writesItemsToFile(@New(Jim.class) Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");

		new ListWriter(file).write("a", "b", "c");

		assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
	}

}
// end::user_guide[]
// @formatter:on
