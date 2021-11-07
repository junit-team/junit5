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
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Test that {@link TempDir temporary directories} are not deleted if set for
 * {@link CleanupMode#NEVER}, and deletes any {@link TempDir temporary directories} if set for
 * {@link CleanupMode#ALWAYS}. hghg
 *
 * @since 5.9
 *
 * @see CleanupMode
 * @see TempDir
 */
class TempDirectoryCleanupTests extends AbstractJupiterTestEngineTests {

	private static File defaultDir;
	private static File neverDir;
	private static File alwaysDir;
	private static File onSuccessDir;

	/**
	 * Ensure the default cleanup modes is ALWAYS.
	 */
	@Test
	void testCleanupModeDefault() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(DefaultCase.class, "testDefault")).build();
		executeTests(request);

		assertFalse(defaultDir.exists());
	}

	/**
	 * Ensure that NEVER cleanup modes are obeyed.
	 */
	@Test
	void testCleanupModeNever() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(NeverCase.class, "testNever")).build();
		executeTests(request);

		assertFalse(neverDir.exists());
	}

	/**
	 * Ensure that ALWAYS cleanup modes are obeyed.
	 */
	@Test
	void testCleanupModeAlways() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(AlwaysCase.class, "testAlways")).build();
		executeTests(request);

		assertFalse(alwaysDir.exists());
	}

	/**
	 * Ensure that ON_SUCCESS cleanup modes are obeyed.
	 */
	@Test
	void testCleanupModeOnSuccess() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(OnSuccessCase.class, "testOnSuccess")).build();
		executeTests(request);

		assertFalse(onSuccessDir.exists());
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

	static class NeverCase {

		@TempDir(cleanup = NEVER)
		File neverDir;

		@Test
		void testNever() {
			TempDirectoryCleanupTests.neverDir = neverDir;
		}
	}

	static class AlwaysCase {

		@TempDir(cleanup = ALWAYS)
		File alwaysDir;

		@Test
		void testAlways() {
			TempDirectoryCleanupTests.alwaysDir = alwaysDir;
		}
	}

	static class OnSuccessCase {

		@TempDir(cleanup = ON_SUCCESS)
		File onSuccessDirDir;

		@Test
		void testOnSuccess() {
			TempDirectoryCleanupTests.onSuccessDir = onSuccessDirDir;
		}
	}

}
