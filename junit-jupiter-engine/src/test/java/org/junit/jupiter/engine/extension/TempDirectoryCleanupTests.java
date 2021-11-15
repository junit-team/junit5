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

		private static File defaultFieldDir;
		private static File neverFieldDir;
		private static File alwaysFieldDir;
		private static File onSuccessFailingFieldDir;
		private static File onSuccessPassingFieldDir;

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

			assertFalse(defaultFieldDir.exists());
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

			assertTrue(neverFieldDir.exists());
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

			assertFalse(alwaysFieldDir.exists());
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

			assertFalse(onSuccessPassingFieldDir.exists());
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

			assertTrue(onSuccessFailingFieldDir.exists());
		}

		@AfterAll
		static void afterAll() {
			if (defaultFieldDir != null && defaultFieldDir.exists()) {
				defaultFieldDir.delete();
			}
			if (neverFieldDir != null && neverFieldDir.exists()) {
				neverFieldDir.delete();
			}
			if (alwaysFieldDir != null && alwaysFieldDir.exists()) {
				alwaysFieldDir.delete();
			}
			if (onSuccessFailingFieldDir != null && onSuccessFailingFieldDir.exists()) {
				onSuccessFailingFieldDir.delete();
			}
			if (onSuccessPassingFieldDir != null && onSuccessPassingFieldDir.exists()) {
				onSuccessPassingFieldDir.delete();
			}
		}

		// -------------------------------------------------------------------

		static class DefaultFieldCase {

			@TempDir
			File defaultFieldDir;

			@Test
			void testDefaultField() {
				TempDirFieldTests.defaultFieldDir = defaultFieldDir;
			}
		}

		static class NeverFieldCase {

			@TempDir(cleanup = NEVER)
			File neverFieldDir;

			@Test
			void testNeverField() {
				TempDirFieldTests.neverFieldDir = neverFieldDir;
			}
		}

		static class AlwaysFieldCase {

			@TempDir(cleanup = ALWAYS)
			File alwaysFieldDir;

			@Test
			void testAlwaysField() {
				TempDirFieldTests.alwaysFieldDir = alwaysFieldDir;
			}
		}

		static class OnSuccessPassingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			File onSuccessPassingFieldDir;

			@Test
			void testOnSuccessPassingField() {
				TempDirFieldTests.onSuccessPassingFieldDir = onSuccessPassingFieldDir;
			}
		}

		static class OnSuccessFailingFieldCase {

			@TempDir(cleanup = ON_SUCCESS)
			File onSuccessFailingFieldDir;

			@Test
			void testOnSuccessFailingField() {
				TempDirFieldTests.onSuccessFailingFieldDir = onSuccessFailingFieldDir;
				fail();
			}
		}

	}

	@Nested
	class TempDirParameterTests {

		private static File defaultParameterDir;
		private static File neverParameterDir;
		private static File alwaysParameterDir;
		private static File onSuccessFailingParameterDir;
		private static File onSuccessPassingParameterDir;

		/**
		 * Ensure the cleanup modes defaults to ALWAYS for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeDefaultParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(DefaultParameterCase.class, "testDefaultParameter", "java.io.File")).build();
			executeTests(request);

			assertFalse(defaultParameterDir.exists());
		}

		/**
		 * Ensure that NEVER cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeNeverParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(NeverParameterCase.class, "testNeverParameter", "java.io.File")).build();
			executeTests(request);

			assertTrue(neverParameterDir.exists());
		}

		/**
		 * Ensure that ALWAYS cleanup modes are obeyed for parameters.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeAlwaysParameter() {
			LauncherDiscoveryRequest request = request().selectors(
				selectMethod(AlwaysParameterCase.class, "testAlwaysParameter", "java.io.File")).build();
			executeTests(request);

			assertFalse(alwaysParameterDir.exists());
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for passing parameter tests.
		 * <p/>
		 * Expect the TempDir to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessPassingParameter() {
			LauncherDiscoveryRequest request = request().selectors(selectMethod(OnSuccessPassingParameterCase.class,
				"testOnSuccessPassingParameter", "java.io.File")).build();
			executeTests(request);

			assertFalse(onSuccessPassingParameterDir.exists());
		}

		/**
		 * Ensure that ON_SUCCESS cleanup modes are obeyed for failing parameter tests.
		 * <p/>
		 * Expect the TempDir not to be cleaned up.
		 */
		@Test
		void testCleanupModeOnSuccessFailingParameter() {
			LauncherDiscoveryRequest request = request().selectors(selectMethod(OnSuccessFailingParameterCase.class,
				"testOnSuccessFailingParameter", "java.io.File")).build();
			executeTests(request);

			assertTrue(onSuccessFailingParameterDir.exists());
		}

		@AfterAll
		static void afterAll() {
			if (defaultParameterDir != null && defaultParameterDir.exists()) {
				defaultParameterDir.delete();
			}
			if (neverParameterDir != null && neverParameterDir.exists()) {
				neverParameterDir.delete();
			}
			if (alwaysParameterDir != null && alwaysParameterDir.exists()) {
				alwaysParameterDir.delete();
			}
			if (onSuccessFailingParameterDir != null && onSuccessFailingParameterDir.exists()) {
				onSuccessFailingParameterDir.delete();
			}
			if (onSuccessPassingParameterDir != null && onSuccessPassingParameterDir.exists()) {
				onSuccessPassingParameterDir.delete();
			}
		}

		// -------------------------------------------------------------------

		static class DefaultParameterCase {

			@Test
			void testDefaultParameter(@TempDir File defaultParameterDir) {
				TempDirParameterTests.defaultParameterDir = defaultParameterDir;
			}
		}

		static class NeverParameterCase {

			@Test
			void testNeverParameter(@TempDir(cleanup = NEVER) File neverParameterDir) {
				TempDirParameterTests.neverParameterDir = neverParameterDir;
			}
		}

		static class AlwaysParameterCase {

			@Test
			void testAlwaysParameter(@TempDir(cleanup = ALWAYS) File alwaysParameterDir) {
				TempDirParameterTests.alwaysParameterDir = alwaysParameterDir;
			}
		}

		static class OnSuccessPassingParameterCase {

			@Test
			void testOnSuccessPassingParameter(@TempDir(cleanup = ON_SUCCESS) File onSuccessPassingParameterDir) {
				TempDirParameterTests.onSuccessPassingParameterDir = onSuccessPassingParameterDir;
			}
		}

		static class OnSuccessFailingParameterCase {

			@Test
			void testOnSuccessFailingParameter(@TempDir(cleanup = ON_SUCCESS) File onSuccessFailingParameterDir) {
				TempDirParameterTests.onSuccessFailingParameterDir = onSuccessFailingParameterDir;
				fail();
			}
		}

	}

}
