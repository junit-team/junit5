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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.support.io.TempDirectory;
import org.junit.jupiter.api.support.io.TempDirectory.TempDir;

// tag::user_guide[]
class TempDirectoryWithCustomFileSystemDemo {

	private static FileSystem fileSystem;

	@BeforeAll
	static void createFileSystem() {
		fileSystem = Jimfs.newFileSystem(Configuration.unix());
	}

	@AfterAll
	static void closeFileSystem() throws Exception {
		fileSystem.close();
	}

	@RegisterExtension
	Extension tempDirectory = TempDirectory.createInCustomDirectory(() -> fileSystem.getPath("/"));

	@Test
	void writesItemsToFile(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");

		new ListWriter(file).write("a", "b", "c");

		assertEquals(singletonList("a,b,c"), Files.readAllLines(file));
	}

}
// end::user_guide[]
