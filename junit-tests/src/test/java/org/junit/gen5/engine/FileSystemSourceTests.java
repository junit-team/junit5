/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.FileSystemSource.FilePosition;

class FileSystemSourceTests {

	@Test
	void nullSourceFileOrDirectoryYieldsException() {
		assertThrows(PreconditionViolationException.class, () -> {
			new FileSystemSource(null);
		});
	}

	@Test
	void directory() {
		File directory = new File(".");
		FileSystemSource source = new FileSystemSource(directory);

		assertTrue(source.isDirectory());
		assertFalse(source.isFile());
		assertFalse(source.isFilePosition());
		assertFalse(source.isJavaClass());
		assertFalse(source.isJavaMethod());

		assertEquals(directory, source.getFile());
		assertThat(source.getPosition()).isEmpty();

		assertEquals(directory.getAbsolutePath(), source.toString());
	}

	@Test
	void fileWithoutPosition() throws Exception {
		File file = spy(new File("test.txt"));
		when(file.isDirectory()).thenReturn(false);
		when(file.isFile()).thenReturn(true);

		FileSystemSource source = new FileSystemSource(file);

		assertFalse(source.isDirectory());
		assertTrue(source.isFile());
		assertFalse(source.isFilePosition());
		assertFalse(source.isJavaClass());
		assertFalse(source.isJavaMethod());

		assertEquals(file, source.getFile());
		assertThat(source.getPosition()).isEmpty();

		assertEquals(file.getAbsolutePath(), source.toString());
	}

	@Test
	void fileWithPosition() throws Exception {
		File file = new File("test.txt");
		FilePosition position = new FilePosition(42, 23);

		FileSystemSource source = new FileSystemSource(file, position);

		assertTrue(source.isFilePosition());
		assertEquals(file, source.getFile());
		assertThat(source.getPosition()).hasValue(position);
		assertEquals(file.getAbsolutePath() + " [42:23]", source.toString());
	}

}
