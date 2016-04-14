/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.api.Assertions.assertSame;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.junit5.descriptor.ClassBasedContainerExtensionContext;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.mockito.Mockito;

/**
 * Microtests for {@link JUnit5EngineExecutionContext}
 */
class JUnit5EngineExecutionContextTests {

	private JUnit5EngineExecutionContext originalContext;
	private EngineExecutionListener engineExecutionListener;

	@BeforeEach
	void initOriginalContext() {
		engineExecutionListener = Mockito.mock(EngineExecutionListener.class);
		originalContext = new JUnit5EngineExecutionContext(engineExecutionListener);
	}

	@Test
	void executionListenerIsHandedOnWhenContextIsExtended() {
		assertSame(engineExecutionListener, originalContext.getExecutionListener());
		JUnit5EngineExecutionContext newContext = originalContext.extend().build();
		assertSame(engineExecutionListener, newContext.getExecutionListener());
	}

	@Test
	void extendWithAllAttributes() {
		ClassBasedContainerExtensionContext extensionContext = new ClassBasedContainerExtensionContext(null, null,
			null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.newRootRegistryWithDefaultExtensions();
		TestInstanceProvider testInstanceProvider = Mockito.mock(TestInstanceProvider.class);
		JUnit5EngineExecutionContext newContext = originalContext.extend() //
				.withExtensionContext(extensionContext) //
				.withExtensionRegistry(extensionRegistry) //
				.withTestInstanceProvider(testInstanceProvider) //
				.build();

		assertSame(extensionContext, newContext.getExtensionContext());
		assertSame(extensionRegistry, newContext.getExtensionRegistry());
		assertSame(testInstanceProvider, newContext.getTestInstanceProvider());
	}

	@Test
	void canOverrideAttributeWhenContextIsExtended() {
		ClassBasedContainerExtensionContext extensionContext = new ClassBasedContainerExtensionContext(null, null,
			null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.newRootRegistryWithDefaultExtensions();
		TestInstanceProvider testInstanceProvider = Mockito.mock(TestInstanceProvider.class);
		ClassBasedContainerExtensionContext newExtensionContext = new ClassBasedContainerExtensionContext(
			extensionContext, null, null);

		JUnit5EngineExecutionContext newContext = originalContext.extend() //
				.withExtensionContext(extensionContext) //
				.withExtensionRegistry(extensionRegistry) //
				.withTestInstanceProvider(testInstanceProvider) //
				.build() //
				.extend() //
				.withExtensionContext(newExtensionContext) //
				.build();

		assertSame(newExtensionContext, newContext.getExtensionContext());
		assertSame(extensionRegistry, newContext.getExtensionRegistry());
		assertSame(testInstanceProvider, newContext.getTestInstanceProvider());
	}
}
