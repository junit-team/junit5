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

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
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
 * @see CleanupMode
 * @see TempDir
 * @since 5.9
 */
class TempDirectoryCleanupTests extends AbstractJupiterTestEngineTests {

	@Nested
	class TempDirFieldTests {

		private static Path defaultFieldDir;
		private static Path neverFieldDir;
		private static Path alwaysFieldDir;
		private static Path onSuccessFailingFieldDir;
		private static Path onSuccessPassingFieldDir;

		/**
		 * Ensure the cleanup modes defaults to ALWAYS for fields.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeDefaultField() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(DefaultFieldCase.class, "testDefaultField")).build();
			executeTests(request);

			assertFalse(exists(defaultFieldDir));
		}

		/**
		 * Ensure that NEVER cleanup modes are obeyed for fields.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeNeverField() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(NeverFieldCase.class, "testNeverField")).build();
			executeTests(request);

			assertTrue(exists(neverFieldDir));
		}

		/**
		 * Ensure that ALWAYS cleanup modes are obeyed for fields.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeAlwaysField() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(AlwaysFieldCase.class, "testAlwaysField")).build();
			executeTests(request);

			assertFalse(exists(alwaysFieldDir));
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing field tests.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessPassingField() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(OnSuccessPassingFieldCase.class, "testOnSuccessPassingField")).build();
			executeTests(request);

			assertFalse(exists(onSuccessPassingFieldDir));
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing field tests.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessFailingField() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(OnSuccessFailingFieldCase.class, "testOnSuccessFailingField")).build();
			executeTests(request);

			assertTrue(exists(onSuccessFailingFieldDir));
		}

		@AfterAll
		static void afterAll() throws IOException {
			deleteIfExists(defaultFieldDir);
			deleteIfExists(neverFieldDir);
			deleteIfExists(alwaysFieldDir);
			deleteIfExists(onSuccessFailingFieldDir);
			deleteIfExists(onSuccessPassingFieldDir);
		}

		// -------------------------------------------------------------------

		static class DefaultFieldCase {

			@TempDir
			Path defaultFieldDir;

			@Test
			void testDefaultField() {
				TempDirFieldTests.defaultFieldDir = defaultFieldDir;
			}
		}

		static class NeverFieldCase {

			@TempDir(cleanup = NEVER)
			Path neverFieldDir;

			@Test
			void testNeverField() {
				TempDirFieldTests.neverFieldDir = neverFieldDir;
			}
		}

		static class AlwaysFieldCase {

			@TempDir(cleanup = ALWAYS)
			Path alwaysFieldDir;

			@Test
			void testAlwaysField() {
				TempDirFieldTests.alwaysFieldDir = alwaysFieldDir;
			}
		}

		static class OnSuccessPassingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			Path onSuccessPassingFieldDir;

			@Test
			void testOnSuccessPassingField() {
				TempDirFieldTests.onSuccessPassingFieldDir = onSuccessPassingFieldDir;
			}
		}

		static class OnSuccessFailingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			Path onSuccessFailingFieldDir;

			@Test
			void testOnSuccessFailingField() {
				TempDirFieldTests.onSuccessFailingFieldDir = onSuccessFailingFieldDir;
				fail();
			}
		}

	}

	@Nested
	class TempDirParameterTests {

		private static Path defaultParameterDir;
		private static Path neverParameterDir;
		private static Path alwaysParameterDir;
		private static Path onSuccessFailingParameterDir;
		private static Path onSuccessPassingParameterDir;

		/**
		 * Ensure the cleanup modes defaults to ALWAYS for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeDefaultParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(DefaultParameterCase.class, "testDefaultParameter", "java.nio.file.Path")).build();
			executeTests(request);

			assertFalse(exists(defaultParameterDir));
		}

		/**
		 * Ensure that NEVER cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeNeverParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(NeverParameterCase.class, "testNeverParameter", "java.nio.file.Path")).build();
			executeTests(request);

			assertTrue(exists(neverParameterDir));
		}

		/**
		 * Ensure that ALWAYS cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeAlwaysParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(AlwaysParameterCase.class, "testAlwaysParameter", "java.nio.file.Path")).build();
			executeTests(request);

			assertFalse(exists(alwaysParameterDir));
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing parameter tests.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessPassingParameter() {
			LauncherDiscoveryRequest request = request().selectors(selectMethod(OnSuccessPassingParameterCase.class,
				"testOnSuccessPassingParameter", "java.nio.file.Path")).build();
			executeTests(request);

			assertFalse(exists(onSuccessPassingParameterDir));
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing parameter tests.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessFailingParameter() {
			LauncherDiscoveryRequest request = request().selectors(selectMethod(OnSuccessFailingParameterCase.class,
				"testOnSuccessFailingParameter", "java.nio.file.Path")).build();
			executeTests(request);

			assertTrue(exists(onSuccessFailingParameterDir));
		}

		@AfterAll
		static void afterAll() throws IOException {
			deleteIfExists(defaultParameterDir);
			deleteIfExists(neverParameterDir);
			deleteIfExists(alwaysParameterDir);
			deleteIfExists(onSuccessFailingParameterDir);
			deleteIfExists(onSuccessPassingParameterDir);
		}

		// -------------------------------------------------------------------

		static class DefaultParameterCase {

			@Test
			void testDefaultParameter(@TempDir Path defaultParameterDir) {
				TempDirParameterTests.defaultParameterDir = defaultParameterDir;
			}
		}

		static class NeverParameterCase {

			@Test
			void testNeverParameter(@TempDir(cleanup = NEVER) Path neverParameterDir) {
				TempDirParameterTests.neverParameterDir = neverParameterDir;
			}
		}

		static class AlwaysParameterCase {

			@Test
			void testAlwaysParameter(@TempDir(cleanup = ALWAYS) Path alwaysParameterDir) {
				TempDirParameterTests.alwaysParameterDir = alwaysParameterDir;
			}
		}

		static class OnSuccessPassingParameterCase {

			@Test
			void testOnSuccessPassingParameter(@TempDir(cleanup = ON_SUCCESS) Path onSuccessPassingParameterDir) {
				TempDirParameterTests.onSuccessPassingParameterDir = onSuccessPassingParameterDir;
			}
		}

		static class OnSuccessFailingParameterCase {

			@Test
			void testOnSuccessFailingParameter(@TempDir(cleanup = ON_SUCCESS) Path onSuccessFailingParameterDir) {
				TempDirParameterTests.onSuccessFailingParameterDir = onSuccessFailingParameterDir;
				fail();
			}
		}

	}

}
