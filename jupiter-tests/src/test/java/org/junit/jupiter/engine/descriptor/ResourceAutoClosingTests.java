/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.launcher.core.NamespacedHierarchicalStoreProviders;
import org.junit.platform.testkit.engine.ExecutionRecorder;

class ResourceAutoClosingTests {

	private final JupiterConfiguration configuration = mock();
	private final ExtensionRegistry extensionRegistry = mock();
	private final JupiterEngineDescriptor testDescriptor = mock();
	private final LauncherStoreFacade launcherStoreFacade = new LauncherStoreFacade(
		NamespacedHierarchicalStoreProviders.dummyNamespacedHierarchicalStore());

	// TODO when config name is renamed, update the test name
	@Test
	void shouldCloseAutoCloseableWhenAutoCloseEnabledIsTrue() throws Exception {
		AutoCloseableResource resource = new AutoCloseableResource();
		when(configuration.isAutoCloseEnabled()).thenReturn(true);

		ExtensionContext extensionContext = new JupiterEngineExtensionContext(null, testDescriptor, configuration,
			extensionRegistry, launcherStoreFacade);
		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
		store.put("resource", resource);

		((AutoCloseable) extensionContext).close();

		assertThat(resource.closed).isTrue();
	}

	@Test
	void shouldNotCloseAutoCloseableWhenAutoCloseEnabledIsFalse() throws Exception {
		AutoCloseableResource resource = new AutoCloseableResource();
		when(configuration.isAutoCloseEnabled()).thenReturn(false);

		ExtensionContext extensionContext = new JupiterEngineExtensionContext(null, testDescriptor, configuration,
			extensionRegistry, launcherStoreFacade);
		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
		store.put("resource", resource);

		((AutoCloseable) extensionContext).close();

		assertThat(resource.closed).isFalse();
	}

	@Test
	void shouldLogWarningWhenClosingResourceImplementsCloseableResourceButNotAutoCloseableAndConfigIsTrue(
			@TrackLogRecords LogRecordListener listener) throws Exception {
		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		CloseableResource resource = new CloseableResource();
		String msg = "Type implements CloseableResource but not AutoCloseable: " + resource.getClass().getName();
		when(configuration.isAutoCloseEnabled()).thenReturn(true);

		ExtensionContext extensionContext = new JupiterEngineExtensionContext(executionRecorder, testDescriptor,
			configuration, extensionRegistry, launcherStoreFacade);
		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
		store.put("resource", resource);

		((AutoCloseable) extensionContext).close();
		assertThat(listener.stream(Level.WARNING)).map(LogRecord::getMessage).anyMatch(msg::equals);
		assertThat(resource.closed).isTrue();
	}

	@Test
	void shouldNotLogWarningWhenClosingResourceImplementsCloseableResourceAndAutoCloseableAndConfigIsFalse(
			@TrackLogRecords LogRecordListener listener) throws Exception {
		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		CloseableResource resource = new CloseableResource();
		String msg = "Type implements CloseableResource but not AutoCloseable: " + resource.getClass().getName();
		when(configuration.isAutoCloseEnabled()).thenReturn(false);

		ExtensionContext extensionContext = new JupiterEngineExtensionContext(executionRecorder, testDescriptor,
			configuration, extensionRegistry, launcherStoreFacade);
		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
		store.put("resource", resource);

		((AutoCloseable) extensionContext).close();
		assertThat(listener.stream(Level.WARNING)).map(LogRecord::getMessage).noneMatch(msg::equals);
		assertThat(resource.closed).isTrue();
	}

	static class AutoCloseableResource implements AutoCloseable {
		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
		}
	}

	@SuppressWarnings("deprecation")
	static class CloseableResource implements ExtensionContext.Store.CloseableResource {
		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
		}
	}
}
