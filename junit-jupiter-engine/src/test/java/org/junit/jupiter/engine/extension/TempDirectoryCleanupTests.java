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
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirStrategy;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Test that {@link TempDirStrategy} does not delete {@link TempDir temporary directories} if set for
 * {@link TempDirStrategy.CleanupMode#NEVER}, and deletes any {@link TempDir temporary directories} if set for
 * {@link TempDirStrategy.CleanupMode#ALWAYS}.
 *
 * @since 5.9
 *
 * @see TempDirStrategy
 * @see TempDir
 */
class TempDirectoryCleanupTests extends AbstractJupiterTestEngineTests {

	private static File defaultDir;
	private static File neverDir;
	private static File alwaysDir;

	@Test
	void testCleanupModeDefault() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(DefaultCase.class, "testDefault")).build();
		executeTests(request);

		assertFalse(defaultDir.exists());
	}

	@Test
	void testCleanupModeNever() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(NeverCase.class, "testNever")).build();
		executeTests(request);

		assertTrue(neverDir.exists());
	}

	@Test
	void testCleanupModeAlways() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(AlwaysCase.class, "testAlways")).build();
		executeTests(request);

		assertFalse(alwaysDir.exists());
	}

	// -------------------------------------------------------------------

	static class DefaultCase {

		@TempDir
		File defaultDir;

		@Test
		void testDefault() {
			TempDirectoryCleanupTests.defaultDir = defaultDir;
		}
	}

	@TempDirStrategy(cleanupMode = TempDirStrategy.CleanupMode.NEVER)
	static class NeverCase {

		@TempDir
		File neverDir;

		@Test
		void testNever() {
			TempDirectoryCleanupTests.neverDir = neverDir;
		}
	}

	@TempDirStrategy(cleanupMode = TempDirStrategy.CleanupMode.ALWAYS)
	static class AlwaysCase {

		@TempDir
		File alwaysDir;

		@Test
		void testAlways() {
			TempDirectoryCleanupTests.alwaysDir = alwaysDir;
		}
	}

}
