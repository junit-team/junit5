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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirStrategy;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests for cleanup of the {@link TempDirectory}
 * when {@link TempDir} is set to {@link TempDirStrategy.CleanupMode#ALWAYS}.
 *
 * @since 5.8
 */
class TempDirectoryCleanupTests extends AbstractJupiterTestEngineTests {

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
