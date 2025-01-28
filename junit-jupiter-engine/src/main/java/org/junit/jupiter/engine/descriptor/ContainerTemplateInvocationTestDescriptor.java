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
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateInvocationTestDescriptor extends JupiterTestDescriptor {

	public static final String SEGMENT_TYPE = "container-template-invocation";

	private final ClassBasedTestDescriptor delegate;

	ContainerTemplateInvocationTestDescriptor(ClassBasedTestDescriptor delegate,
			ContainerTemplateInvocationContext invocationContext, int index) {
		super(delegate.getUniqueId(), invocationContext.getDisplayName(index), delegate.getSource().orElse(null),
			delegate.configuration);
		this.delegate = delegate;
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
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
		return delegate.getType();
	}
}
