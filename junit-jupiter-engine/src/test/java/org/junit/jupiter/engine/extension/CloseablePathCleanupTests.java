/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.opentest4j.TestAbortedException;

/**
 * Integration tests for cleanup of the {@link TempDirectory}
 * when {@link TempDir} is set to {@link CleanupMode#ALWAYS} or
 * {@link CleanupMode#NEVER}.
 *
 * @since 5.9
 *
 * @see TempDir
 * @see CleanupMode
 */
class CloseablePathCleanupTests extends AbstractJupiterTestEngineTests {

	/**
	 * Ensure a closeable path is cleaned up for a cleanup mode of ALWAYS.
	 */
	@Test
	void testAlways() throws IOException {
		ExtensionContext extensionContext = mock(ExtensionContext.class);
		TempDirectory.CloseablePath path = TempDirectory.createTempDir(ALWAYS, extensionContext);
		assertTrue(path.get().toFile().exists());

		path.close();
		assertFalse(path.get().toFile().exists());
	}

	/**
	 * Ensure a closeable path is not cleaned up for a cleanup mode of NEVER.
	 */
	@Test
	void testNever() throws IOException {
		ExtensionContext extensionContext = mock(ExtensionContext.class);
		TempDirectory.CloseablePath path = TempDirectory.createTempDir(NEVER, extensionContext);
		assertTrue(path.get().toFile().exists());

		path.close();
		assertTrue(path.get().toFile().exists());
	}

	/**
	 * Ensure a closeable path is not cleaned up for a cleanup mode of ON_SUCCESS if there is a TestAbortedException.
	 */
	@Test
	void testOnSuccessWithTestAbortedException() throws IOException {
		ExtensionContext extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getExecutionException()).thenReturn(Optional.of(new TestAbortedException()));

		TempDirectory.CloseablePath path = TempDirectory.createTempDir(ON_SUCCESS, extensionContext);
		assertTrue(path.get().toFile().exists());

		path.close();
		assertTrue(path.get().toFile().exists());
	}

	/**
	 * Ensure a closeable path is cleaned up for a cleanup mode of ON_SUCCESS if there is no exception.
	 */
	@Test
	void testOnSuccessWithNoTestAbortedException() throws IOException {
		ExtensionContext extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getExecutionException()).thenReturn(Optional.empty());

		TempDirectory.CloseablePath path = TempDirectory.createTempDir(ON_SUCCESS, extensionContext);
		assertTrue(path.get().toFile().exists());
		path.close();
		assertFalse(path.get().toFile().exists());
	}

}
