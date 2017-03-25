/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.jupiter.engine.extension.ExtensionRegistry.createRegistryWithDefaultExtensions;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

/**
 * @since 5.0
 */
@API(Internal)
public class JupiterEngineDescriptor extends EngineDescriptor implements Node<JupiterEngineExecutionContext> {

	public JupiterEngineDescriptor(UniqueId uniqueId) {
		super(uniqueId, "JUnit Jupiter");
	}

	@Override
	public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
		ExtensionRegistry extensionRegistry = createRegistryWithDefaultExtensions(context.getConfigurationParameters());

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(extensionRegistry)
				.build();
		// @formatter:on
	}

}
