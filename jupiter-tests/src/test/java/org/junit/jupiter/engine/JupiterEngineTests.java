/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.core.NamespacedHierarchicalStoreProviders;

/**
 * @since  5.13
 */
public class JupiterEngineTests {

	private final TestDescriptor rootTestDescriptor = mock(JupiterEngineDescriptor.class);

	private final ConfigurationParameters configurationParameters = mock();

	private final EngineExecutionListener engineExecutionListener = mock();

	private final ExecutionRequest executionRequest = mock();

	private final JupiterTestEngine engine = new JupiterTestEngine();

	@BeforeEach
	void setUp() {
		when(executionRequest.getEngineExecutionListener()).thenReturn(engineExecutionListener);
		when(executionRequest.getConfigurationParameters()).thenReturn(configurationParameters);
		when(executionRequest.getRootTestDescriptor()).thenReturn(rootTestDescriptor);
	}

	@Test
	void createExecutionContextWithValidRequest() {
		when(executionRequest.getRequestLevelStore()).thenReturn(
			NamespacedHierarchicalStoreProviders.dummyNamespacedHierarchicalStore());

		JupiterEngineExecutionContext context = engine.createExecutionContext(executionRequest);
		assertThat(context).isNotNull();
	}

	@Test
	void createExecutionContextWithNoParentsRequestLevelStore() {
		when(executionRequest.getRequestLevelStore()).thenReturn(
			NamespacedHierarchicalStoreProviders.dummyNamespacedHierarchicalStoreWithNoParent());

		assertThatThrownBy(() -> engine.createExecutionContext(executionRequest)).isInstanceOf(
			JUnitException.class).hasMessageContaining("Request-level store must have a parent");
	}

}
