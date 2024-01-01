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

import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static org.junit.jupiter.api.io.CleanupMode.NEVER;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

/**
 * Integration tests for cleanup of the {@link TempDirectory} when the {@link CleanupMode} is
 * set to {@link CleanupMode#ALWAYS}, {@link CleanupMode#NEVER}, or {@link CleanupMode#ON_SUCCESS}.
 *
 * @since 5.9
 *
 * @see TempDir
 * @see CleanupMode
 */
class CloseablePathCleanupTests extends AbstractJupiterTestEngineTests {

	private final AnnotatedElementContext elementContext = mock();
	private final ExtensionContext extensionContext = mock();
	private final TempDirFactory factory = spy(TempDirFactory.Standard.INSTANCE);

	private TempDirectory.CloseablePath closeablePath;

	@BeforeEach
	void setUpExtensionContext() {
		var store = new NamespaceAwareStore(new NamespacedHierarchicalStore<>(null), Namespace.GLOBAL);
		when(extensionContext.getStore(any())).thenReturn(store);
	}

	@AfterEach
	void cleanupTempDirectory() throws IOException {
		deleteIfExists(closeablePath.get());
	}

	@Test
	@DisplayName("is cleaned up for a cleanup mode of ALWAYS")
	void always() throws IOException {
		closeablePath = TempDirectory.createTempDir(factory, ALWAYS, elementContext, extensionContext);
		assertThat(closeablePath.get()).exists();

		closeablePath.close();
		assertThat(closeablePath.get()).doesNotExist();
		verify(factory).close();
	}

	@Test
	@DisplayName("is not cleaned up for a cleanup mode of NEVER")
	void never() throws IOException {
		closeablePath = TempDirectory.createTempDir(factory, NEVER, elementContext, extensionContext);
		assertThat(closeablePath.get()).exists();

		closeablePath.close();
		assertThat(closeablePath.get()).exists();
		verify(factory).close();
	}

	@Test
	@DisplayName("is not cleaned up for a cleanup mode of ON_SUCCESS, if there is an exception")
	void onSuccessWithException() throws IOException {
		when(extensionContext.getExecutionException()).thenReturn(Optional.of(new Exception()));

		closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementContext, extensionContext);
		assertThat(closeablePath.get()).exists();

		closeablePath.close();
		assertThat(closeablePath.get()).exists();
		verify(factory).close();
	}

	@Test
	@DisplayName("is cleaned up for a cleanup mode of ON_SUCCESS, if there is no exception")
	void onSuccessWithNoException() throws IOException {
		when(extensionContext.getExecutionException()).thenReturn(Optional.empty());

		closeablePath = TempDirectory.createTempDir(factory, ON_SUCCESS, elementContext, extensionContext);
		assertThat(closeablePath.get()).exists();

		closeablePath.close();
		assertThat(closeablePath.get()).doesNotExist();
		verify(factory).close();
	}

}
