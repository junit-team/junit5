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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.launcher.core.NamespacedHierarchicalStoreProviders;

class ResourceAutoClosingTests {

	private final JupiterConfiguration configuration = mock();
	private final ExtensionRegistry extensionRegistry = mock();
	private final JupiterEngineDescriptor testDescriptor = mock();
	private final LauncherStoreFacade launcherStoreFacade = new LauncherStoreFacade(
		NamespacedHierarchicalStoreProviders.dummyNamespacedHierarchicalStore());

	@BeforeEach
	void setUp() {
		when(testDescriptor.getConfiguration()).thenReturn(configuration);
	}

	@Test
	void shouldCloseAutoCloseableWhenAutoCloseEnabledIsTrue() throws Exception {
		AutoCloseableResource resource = new AutoCloseableResource();
		when(configuration.isAutoCloseEnabled()).thenReturn(true);

		ExtensionContext extensionContext = new JupiterEngineExtensionContext(null, testDescriptor, configuration,
			extensionRegistry, launcherStoreFacade);
		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.StoreScope.EXECUTION_REQUEST,
			ExtensionContext.Namespace.GLOBAL);
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

		ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.StoreScope.EXECUTION_REQUEST,
			ExtensionContext.Namespace.GLOBAL);
		store.put("resource", resource);

		((AutoCloseable) extensionContext).close();

		assertThat(resource.closed).isFalse();
	}

	static class AutoCloseableResource implements AutoCloseable {
		private boolean closed = false;

		@Override
		public void close() {
			closed = true;
		}
	}

	static class AutoCloseableResourceStoreUsingExtension implements BeforeAllCallback {
		@Override
		public void beforeAll(ExtensionContext context) {
			var store = context.getStore(ExtensionContext.Namespace.GLOBAL);
			store.put("resource", new AutoCloseableResource());
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ExtendWith(AutoCloseableResourceStoreUsingExtension.class)
	static class AutoCloseableTestCase {
		@Test
		void dummyTest() {
		}
	}
}
