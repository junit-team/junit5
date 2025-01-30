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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateInvocationTestDescriptor extends JupiterTestDescriptor {

	public static final String SEGMENT_TYPE = "container-template-invocation";

	ContainerTemplateInvocationTestDescriptor(UniqueId uniqueId, String displayName, TestSource source,
			JupiterConfiguration configuration) {
		super(uniqueId, displayName, source, configuration);
	}

	@Override
	protected ContainerTemplateInvocationTestDescriptor withUniqueId(UniqueId newUniqueId) {
		return new ContainerTemplateInvocationTestDescriptor(newUniqueId, getDisplayName(), getSource().orElse(null),
			this.configuration);
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		return context.extend().build();
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		Visitor visitor = context.getExecutionListener()::dynamicTestRegistered;
		getChildren().forEach(child -> child.accept(visitor));
		return context;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}
}
