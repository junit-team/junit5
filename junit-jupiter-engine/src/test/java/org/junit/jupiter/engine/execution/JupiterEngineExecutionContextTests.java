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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;

/**
 * Unit tests for {@link JupiterEngineExecutionContext}.
 *
 * @since 5.0
 */
class JupiterEngineExecutionContextTests {

	private final JupiterConfiguration configuration = mock(JupiterConfiguration.class);

	private final EngineExecutionListener engineExecutionListener = mock(EngineExecutionListener.class);

	private final JupiterEngineExecutionContext originalContext = new JupiterEngineExecutionContext(
		engineExecutionListener, configuration);

	@Test
	void executionListenerIsHandedOnWhenContextIsExtended() {
		assertSame(engineExecutionListener, originalContext.getExecutionListener());
		JupiterEngineExecutionContext newContext = originalContext.extend().build();
		assertSame(engineExecutionListener, newContext.getExecutionListener());
	}

	@Test
	void extendWithAllAttributes() {
		UniqueId uniqueId = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]");
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(uniqueId, getClass(), configuration);
		ClassExtensionContext extensionContext = new ClassExtensionContext(null, null, classTestDescriptor,
			configuration, null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.createRegistryWithDefaultExtensions(configuration);
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
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(uniqueId, getClass(), configuration);
		ClassExtensionContext extensionContext = new ClassExtensionContext(null, null, classTestDescriptor,
			configuration, null);
		ExtensionRegistry extensionRegistry = ExtensionRegistry.createRegistryWithDefaultExtensions(configuration);
		TestInstanceProvider testInstanceProvider = mock(TestInstanceProvider.class);
		ClassExtensionContext newExtensionContext = new ClassExtensionContext(extensionContext, null,
			classTestDescriptor, configuration, null);

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

	@Test
	@TrackLogRecords
	void closeAttemptExceptionWillBeThrownDownTheCallStack(LogRecordListener logRecordListener) throws Exception {
		ExtensionContext failingExtensionContext = mock(ExtensionContext.class,
			withSettings().extraInterfaces(AutoCloseable.class));
		Exception expectedException = new Exception("test message");
		doThrow(expectedException).when(((AutoCloseable) failingExtensionContext)).close();

		JupiterEngineExecutionContext newContext = originalContext.extend() //
				.withExtensionContext(failingExtensionContext) //
				.build();

		Exception actualException = assertThrows(Exception.class, newContext::close);

		assertSame(expectedException, actualException);
		assertThat(logRecordListener.stream(JupiterEngineExecutionContext.class, Level.SEVERE)) //
				.extracting(LogRecord::getMessage) //
				.containsOnly("Caught exception while closing extension context: " + failingExtensionContext);
	}

}
