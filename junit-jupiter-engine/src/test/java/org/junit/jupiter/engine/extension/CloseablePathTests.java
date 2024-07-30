/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.DEFAULT;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Integration tests for the creation and cleanup of the {@link TempDirectory}.
 *
 * @since 5.9
 */
@DisplayName("Temporary directory")
class CloseablePathTests extends AbstractJupiterTestEngineTests {

	private final AnnotatedElementContext elementContext = mock();
	private final ExtensionContext extensionContext = mock();

	private TempDirectory.CloseablePath closeablePath;

	@BeforeEach
	void setUpExtensionContext() {
		var store = new NamespaceAwareStore(new NamespacedHierarchicalStore<>(null), Namespace.GLOBAL);
		when(extensionContext.getStore(any())).thenReturn(store);
	}

	/**
	 * Integration tests for the creation of the {@link TempDirectory} based on the different result
	 * that {@link TempDirFactory#createTempDirectory(AnnotatedElementContext, ExtensionContext)} may provide.
	 *
	 * @since 5.11
	 *
	 * @see TempDirFactory
	 */
	@Nested
	@DisplayName("creation")
	class Creation {

		private Path root;

		@BeforeEach
		void setUpRootFolder() throws IOException {
			root = createTempDirectory("root");
		}

		@AfterEach
		void cleanupRoot() throws IOException {
			delete(root);
		}

		@Test
		@DisplayName("succeeds if the factory returns a directory")
		void factoryReturnsDirectory() throws Exception {
			TempDirFactory factory = (elementContext, extensionContext) -> createDirectory(root.resolve("directory"));

			closeablePath = TempDirectory.createTempDir(factory, DEFAULT, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			delete(closeablePath.get());
		}

		@Test
		@DisplayName("succeeds if the factory returns a symbolic link to a directory")
		void factoryReturnsSymbolicLinkToDirectory() throws Exception {
			Path directory = createDirectory(root.resolve("directory"));
			TempDirFactory factory = (elementContext,
					extensionContext) -> createSymbolicLink(root.resolve("symbolicLink"), directory);

			closeablePath = TempDirectory.createTempDir(factory, DEFAULT, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			delete(closeablePath.get());
			delete(directory);
		}

		@Test
		@DisplayName("fails if the factory returns null")
		void factoryReturnsNull() {
			TempDirFactory factory = (elementContext, extensionContext) -> null;

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementContext, extensionContext));
		}

		@Test
		@DisplayName("fails if the factory returns a file")
		void factoryReturnsFile() throws IOException {
			Path file = createFile(root.resolve("file"));
			TempDirFactory factory = (elementContext, extensionContext) -> file;

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementContext, extensionContext));

			delete(file);
		}

		@Test
		@DisplayName("fails if the factory returns a symbolic link to a file")
		void factoryReturnsSymbolicLinkToFile() throws IOException {
			Path file = createFile(root.resolve("directory"));
			Path symbolicLink = createSymbolicLink(root.resolve("symbolicLink"), file);
			TempDirFactory factory = (elementContext, extensionContext) -> symbolicLink;

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementContext, extensionContext));

			delete(symbolicLink);
			delete(file);
		}

		private static void assertThatExtensionConfigurationExceptionIsThrownBy(ThrowingCallable callable) {
			assertThatExceptionOfType(ExtensionConfigurationException.class)//
					.isThrownBy(callable)//
					.withMessage("Failed to create default temp directory")//
					.withCauseInstanceOf(PreconditionViolationException.class)//
					.havingCause().withMessage("temp directory must be a directory");
		}

	}

	/**
	 * Integration tests for cleanup of the {@link TempDirectory} when the {@link CleanupMode} is
	 * set to {@link CleanupMode#ALWAYS}, {@link CleanupMode#NEVER}, or {@link CleanupMode#ON_SUCCESS}.
	 *
	 * @since 5.9
	 *
	 * @see TempDir
	 * @see CleanupMode
	 */
	@Nested
	@DisplayName("cleanup")
	class Cleanup {

		private final TempDirFactory factory = spy(TempDirFactory.Standard.INSTANCE);

		@AfterEach
		void cleanupTempDirectory() throws IOException {
			deleteIfExists(closeablePath.get());
		}

		@Test
		@DisplayName("is done for a cleanup mode of ALWAYS")
		void always() throws IOException {
			closeablePath = TempDirectory.createTempDir(factory, ALWAYS, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();
			assertThat(closeablePath.get()).doesNotExist();
			verify(factory).close();
		}

		@Test
		@DisplayName("is not done for a cleanup mode of NEVER")
		void never() throws IOException {
			closeablePath = TempDirectory.createTempDir(factory, NEVER, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();
			assertThat(closeablePath.get()).exists();
			verify(factory).close();
		}

		@Test
		@DisplayName("is not done for a cleanup mode of ON_SUCCESS, if there is an exception")
		void onSuccessWithException() throws IOException {
			when(extensionContext.getExecutionException()).thenReturn(Optional.of(new Exception()));

			closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();
			assertThat(closeablePath.get()).exists();
			verify(factory).close();
		}

		@Test
		@DisplayName("is done for a cleanup mode of ON_SUCCESS, if there is no exception")
		void onSuccessWithNoException() throws IOException {
			when(extensionContext.getExecutionException()).thenReturn(Optional.empty());

			closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();
			assertThat(closeablePath.get()).doesNotExist();
			verify(factory).close();
		}

	}

}
