/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.engine.support.hierarchical.Container;

/**
 * @since 5.0
 */
@API(Internal)
public class JUnit5EngineDescriptor extends EngineDescriptor implements Container<JUnit5EngineExecutionContext> {

	public JUnit5EngineDescriptor(UniqueId uniqueId) {
		super(uniqueId, "JUnit 5");
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) {
		return context.extend().withExtensionRegistry(ExtensionRegistry.newRootRegistryWithDefaultExtensions()).build();
	}

}
