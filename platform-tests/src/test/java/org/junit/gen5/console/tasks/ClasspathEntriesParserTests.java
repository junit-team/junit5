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

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.gen5.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.gen5.api.Test;

/**
 * @since 5.0
 */
public class ClasspathEntriesParserTests {

	@Test
	public void toURLsWithoutEntries() {
		URL[] urls = new ClasspathEntriesParser().toURLs(emptyList());

		assertEquals(0, urls.length);
	}

	@Test
	public void toURLsWithSinglePath() throws Exception {
		Path root = getFileSystemRoot();
		Path path = root.resolve("bin");

		URL[] urls = new ClasspathEntriesParser().toURLs(singletonList(path.toString()));

		assertEquals(1, urls.length);
		assertEquals(path.toUri().toURL(), urls[0]);
	}

	@Test
	public void toURLsWithMultiplePathsInMultipleEntries() throws Exception {
		Path root = getFileSystemRoot();
		Path path1 = root.resolve("foo");
		Path path2 = root.resolve("bar");

		URL[] urls = new ClasspathEntriesParser().toURLs(asList(path1.toString(), path2.toString()));

		assertEquals(2, urls.length);
		assertEquals(path1.toUri().toURL(), urls[0]);
		assertEquals(path2.toUri().toURL(), urls[1]);
	}

	@Test
	public void toURLsWithMultiplePathsInSingleEntry() throws Exception {
		Path root = getFileSystemRoot();
		Path path1 = root.resolve("foo");
		Path path2 = root.resolve("bar");

		URL[] urls = new ClasspathEntriesParser().toURLs(
			singletonList(join(File.pathSeparator, path1.toString(), path2.toString())));

		assertEquals(2, urls.length);
		assertEquals(path1.toUri().toURL(), urls[0]);
		assertEquals(path2.toUri().toURL(), urls[1]);
	}

	private Path getFileSystemRoot() {
		return FileSystems.getDefault().getRootDirectories().iterator().next();
	}

}
