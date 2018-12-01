/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.JupiterTestDescriptor.toExecutionMode;
import static org.junit.jupiter.engine.extension.ExtensionRegistry.createRegistryWithDefaultExtensions;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class JupiterEngineDescriptor extends EngineDescriptor implements Node<JupiterEngineExecutionContext> {

	public static final String ENGINE_ID = "junit-jupiter";
	private final ConfigurationParameters configurationParameters;

	public JupiterEngineDescriptor(UniqueId uniqueId, ConfigurationParameters configurationParameters) {
		super(uniqueId, "JUnit Jupiter");
		this.configurationParameters = configurationParameters;
	}

	@Override
	public ExecutionMode getExecutionMode() {
		return toExecutionMode(ExecutionModeUtils.getDefaultExecutionMode(configurationParameters));
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		ExtensionRegistry extensionRegistry = createRegistryWithDefaultExtensions(context.getConfigurationParameters());
		EngineExecutionListener executionListener = context.getExecutionListener();
		ExtensionContext extensionContext = new JupiterEngineExtensionContext(executionListener, this,
			context.getConfigurationParameters());

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(extensionRegistry)
				.withExtensionContext(extensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public void cleanUp(JupiterEngineExecutionContext context) throws Exception {
		context.close();
	}

}
