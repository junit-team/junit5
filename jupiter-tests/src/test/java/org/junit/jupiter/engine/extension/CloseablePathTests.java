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

import static com.google.common.jimfs.Configuration.unix;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createSymbolicLink;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.DEFAULT;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.jimfs.Jimfs;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

	@Target(METHOD)
	@Retention(RUNTIME)
	@ValueSource(classes = { File.class, Path.class })
	private @interface ElementTypeSource {
	}

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

		@DisplayName("succeeds if the factory returns a directory")
		@ParameterizedTest
		@ElementTypeSource
		void factoryReturnsDirectoryDynamic(Class<?> elementType) throws IOException {
			TempDirFactory factory = (elementContext, extensionContext) -> createDirectory(root.resolve("directory"));

			closeablePath = TempDirectory.createTempDir(factory, DEFAULT, elementType, elementContext,
				extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			delete(closeablePath.get());
		}

		@DisplayName("succeeds if the factory returns a symbolic link to a directory")
		@ParameterizedTest
		@ElementTypeSource
		@DisabledOnOs(WINDOWS)
		void factoryReturnsSymbolicLinkToDirectory(Class<?> elementType) throws IOException {
			Path directory = createDirectory(root.resolve("directory"));
			TempDirFactory factory = (elementContext,
					extensionContext) -> createSymbolicLink(root.resolve("symbolicLink"), directory);

			closeablePath = TempDirectory.createTempDir(factory, DEFAULT, elementType, elementContext,
				extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			delete(closeablePath.get());
			delete(directory);
		}

		@DisplayName("succeeds if the factory returns a directory on a non-default file system for a Path annotated element")
		@Test
		void factoryReturnsDirectoryOnNonDefaultFileSystemWithPath() throws IOException {
			TempDirFactory factory = new JimfsFactory();

			closeablePath = TempDirectory.createTempDir(factory, DEFAULT, Path.class, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			delete(closeablePath.get());
		}

		@DisplayName("fails if the factory returns null")
		@ParameterizedTest
		@ElementTypeSource
		void factoryReturnsNull(Class<?> elementType) throws IOException {
			TempDirFactory factory = spy(new Factory(null));

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementType, elementContext, extensionContext));

			verify(factory).close();
		}

		@DisplayName("fails if the factory returns a file")
		@ParameterizedTest
		@ElementTypeSource
		void factoryReturnsFile(Class<?> elementType) throws IOException {
			Path file = createFile(root.resolve("file"));
			TempDirFactory factory = spy(new Factory(file));

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementType, elementContext, extensionContext));

			verify(factory).close();
			assertThat(file).doesNotExist();
		}

		@DisplayName("fails if the factory returns a symbolic link to a file")
		@ParameterizedTest
		@ElementTypeSource
		@DisabledOnOs(WINDOWS)
		void factoryReturnsSymbolicLinkToFile(Class<?> elementType) throws IOException {
			Path file = createFile(root.resolve("file"));
			Path symbolicLink = createSymbolicLink(root.resolve("symbolicLink"), file);
			TempDirFactory factory = spy(new Factory(symbolicLink));

			assertThatExtensionConfigurationExceptionIsThrownBy(
				() -> TempDirectory.createTempDir(factory, DEFAULT, elementType, elementContext, extensionContext));

			verify(factory).close();
			assertThat(symbolicLink).doesNotExist();

			delete(file);
		}

		@DisplayName("fails if the factory returns a directory on a non-default file system for a File annotated element")
		@Test
		void factoryReturnsDirectoryOnNonDefaultFileSystemWithFile() throws IOException {
			TempDirFactory factory = spy(new JimfsFactory());

			assertThatExceptionOfType(ExtensionConfigurationException.class)//
					.isThrownBy(() -> TempDirectory.createTempDir(factory, DEFAULT, File.class, elementContext,
						extensionContext))//
					.withMessage("Failed to create default temp directory")//
					.withCauseInstanceOf(PreconditionViolationException.class)//
					.havingCause().withMessage("temp directory with non-default file system cannot be injected into "
							+ File.class.getName() + " target");

			verify(factory).close();
		}

		// Mockito spying a lambda fails with: VM does not support modification of given type
		private record Factory(Path path) implements TempDirFactory {

			@Override
			public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext) {
				return path;
			}

		}

		private static class JimfsFactory implements TempDirFactory {

			private final FileSystem fileSystem = Jimfs.newFileSystem(unix());

			@Override
			public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
					throws Exception {
				return createDirectory(fileSystem.getPath("/").resolve("directory"));
			}

			@Override
			public void close() throws IOException {
				TempDirFactory.super.close();
			}
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

		@DisplayName("is done for a cleanup mode of ALWAYS")
		@ParameterizedTest
		@ElementTypeSource
		void always(Class<?> elementType) throws IOException {
			reset(factory);

			closeablePath = TempDirectory.createTempDir(factory, ALWAYS, elementType, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();

			verify(factory).close();
			assertThat(closeablePath.get()).doesNotExist();
		}

		@DisplayName("is not done for a cleanup mode of NEVER")
		@ParameterizedTest
		@ElementTypeSource
		void never(Class<?> elementType) throws IOException {
			reset(factory);

			closeablePath = TempDirectory.createTempDir(factory, NEVER, elementType, elementContext, extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();

			verify(factory).close();
			assertThat(closeablePath.get()).exists();
		}

		@DisplayName("is not done for a cleanup mode of ON_SUCCESS, if there is an exception")
		@ParameterizedTest
		@ElementTypeSource
		void onSuccessWithException(Class<?> elementType) throws IOException {
			reset(factory);

			when(extensionContext.getExecutionException()).thenReturn(Optional.of(new Exception()));

			closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementType, elementContext,
				extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();

			verify(factory).close();
			assertThat(closeablePath.get()).exists();
		}

		@DisplayName("is done for a cleanup mode of ON_SUCCESS, if there is no exception")
		@ParameterizedTest
		@ElementTypeSource
		void onSuccessWithNoException(Class<?> elementType) throws IOException {
			reset(factory);

			when(extensionContext.getExecutionException()).thenReturn(Optional.empty());

			closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementType, elementContext,
				extensionContext);
			assertThat(closeablePath.get()).isDirectory();

			closeablePath.close();

			verify(factory).close();
			assertThat(closeablePath.get()).doesNotExist();
		}

	}

}
