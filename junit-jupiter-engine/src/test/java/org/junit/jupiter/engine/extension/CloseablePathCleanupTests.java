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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirStrategy;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests for cleanup of the {@link TempDirectory}
 * when {@link TempDir} is set to {@link TempDirStrategy.CleanupMode#ALWAYS} or
 * {@link TempDirStrategy.CleanupMode#NEVER}.
 *
 * @since 5.9
 *
 * @see TempDir
 * @see TempDirStrategy
 */
class CloseablePathCleanupTests extends AbstractJupiterTestEngineTests {

	/**
	 * Ensure a closeable path is cleaned up for a cleanup mode of ALWAYS.
	 */
	@Test
	void testAlways() {
		TempDirectory.CloseablePath path = TempDirectory.createTempDir(TempDirStrategy.CleanupMode.ALWAYS);
		assertTrue(path.get().toFile().exists());
		try {
			path.close();
		}
		catch (Exception e) {
			fail(e);
		}
		assertFalse(path.get().toFile().exists());
	}

	/**
	 * Ensure a closeable path is not cleaned up for a cleanup mode of NEVER.
	 */
	@Test
	void testNever() {
		TempDirectory.CloseablePath path = TempDirectory.createTempDir(TempDirStrategy.CleanupMode.NEVER);
		assertTrue(path.get().toFile().exists());
		try {
			path.close();
		}
		catch (Exception e) {
			fail(e);
		}
		assertTrue(path.get().toFile().exists());
	}

}
