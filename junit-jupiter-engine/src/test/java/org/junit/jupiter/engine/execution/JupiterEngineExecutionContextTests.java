/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;

/**
 * Unit tests for {@link JupiterEngineExecutionContext}.
 *
 * @since 5.0
 */
class JupiterEngineExecutionContextTests {

	private final ConfigurationParameters configParams = mock(ConfigurationParameters.class);

	private final EngineExecutionListener engineExecutionListener = mock(EngineExecutionListener.class);

	private final JupiterEngineExecutionContext originalContext = new JupiterEngineExecutionContext(
		engineExecutionListener, configParams);

	@Test
	void executionListenerIsHandedOnWhenContextIsExtended() {
		assertSame(engineExecutionListener, originalContext.getExecutionListener());
		JupiterEngineExecutionContext newContext = originalContext.extend().build();
		assertSame(engineExecutionListener, newContext.getExecutionListener());
	}

	@Test
	void extendWithAllAttributes() {
		UniqueId uniqueId = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]");
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(uniqueId, getClass(), configParams);
		ClassExtensionContext extensionContext = new ClassExtensionContext(null, null, classTestDescriptor,
			configParams, null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.createRegistryWithDefaultExtensions(configParams);
		TestInstanceProvider testInstanceProvider = mock(TestInstanceProvider.class);
		JupiterEngineExecutionContext newContext = originalContext.extend() //
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
		UniqueId uniqueId = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]");
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(uniqueId, getClass(), configParams);
		ClassExtensionContext extensionContext = new ClassExtensionContext(null, null, classTestDescriptor,
			configParams, null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.createRegistryWithDefaultExtensions(configParams);
		TestInstanceProvider testInstanceProvider = mock(TestInstanceProvider.class);
		ClassExtensionContext newExtensionContext = new ClassExtensionContext(extensionContext, null,
			classTestDescriptor, configParams, null);

		JupiterEngineExecutionContext newContext = originalContext.extend() //
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
