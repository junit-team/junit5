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
 * Test that {@link TempDir temporary directories} are not deleted if set for {@link CleanupMode#NEVER},
 * deletes any if set for {@link CleanupMode#ON_SUCCESS} only if the test passes,
 * and deletes any if set for {@link CleanupMode#ALWAYS}.
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
	private static File onSuccessFailingDir;
	private static File onSuccessPassingDir;

	/**
	 * Ensure the cleanup modes defaults to ALWAYS.
	 * <p/>
	 * Expect the TempDir to be cleaned up.
	 */
	@Test
	void testCleanupModeDefault() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(DefaultCase.class, "testDefault")).build();
		executeTests(request);

		assertFalse(defaultDir.exists());
	}

	/**
	 * Ensure that NEVER cleanup modes are obeyed.
	 * <p/>
	 * Expect the TempDir not to be cleaned up.
	 */
	@Test
	void testCleanupModeNever() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(NeverCase.class, "testNever")).build();
		executeTests(request);

		assertTrue(neverDir.exists());
	}

	/**
	 * Ensure that ALWAYS cleanup modes are obeyed.
	 * <p/>
	 * Expect the TempDir to be cleaned up.
	 */
	@Test
	void testCleanupModeAlways() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(AlwaysCase.class, "testAlways")).build();
		executeTests(request);

		assertFalse(alwaysDir.exists());
	}

	/**
	 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing tests.
	 * <p/>
	 * Expect the TempDir to be cleaned up.
	 */
	@Test
	void testCleanupModeOnSuccessPassing() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(OnSuccessPassingCase.class, "testOnSuccessPassing")).build();
		executeTests(request);

		assertFalse(onSuccessPassingDir.exists());
	}

	/**
	 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing tests.
	 * <p/>
	 * Expect the TempDir not to be cleaned up.
	 */
	@Test
	void testCleanupModeOnSuccessFailing() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(OnSuccessFailingCase.class, "testOnSuccessFailing")).build();
		executeTests(request);

		assertTrue(onSuccessFailingDir.exists());
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

	static class OnSuccessPassingCase {

		@TempDir(cleanup = ON_SUCCESS)
		File onSuccessPassingDir;

		@Test
		void testOnSuccessPassing() {
			TempDirectoryCleanupTests.onSuccessPassingDir = onSuccessPassingDir;
		}
	}

	static class OnSuccessFailingCase {

		@TempDir(cleanup = ON_SUCCESS)
		File onSuccessFailingDir;

		@Test
		void testOnSuccessFailing() {
			TempDirectoryCleanupTests.onSuccessFailingDir = onSuccessFailingDir;
			fail();
		}
	}

}
