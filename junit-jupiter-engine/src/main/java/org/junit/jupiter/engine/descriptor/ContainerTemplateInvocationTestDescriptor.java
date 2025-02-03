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
import static org.junit.jupiter.engine.extension.MutableExtensionRegistry.createRegistryFrom;

import java.util.List;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.13
 */
@API(status = INTERNAL, since = "5.13")
public class ContainerTemplateInvocationTestDescriptor extends JupiterTestDescriptor {

	public static final String SEGMENT_TYPE = "container-template-invocation";

	private final ContainerTemplateInvocationContext invocationContext;
	private final int index;

	ContainerTemplateInvocationTestDescriptor(UniqueId uniqueId, ContainerTemplateInvocationContext invocationContext,
			int index, TestSource source, JupiterConfiguration configuration) {
		super(uniqueId, invocationContext.getDisplayName(index), source, configuration);
		this.invocationContext = invocationContext;
		this.index = index;
	}

	@Override
	protected ContainerTemplateInvocationTestDescriptor withUniqueId(UniqueId newUniqueId) {
		return new ContainerTemplateInvocationTestDescriptor(newUniqueId, this.invocationContext, this.index,
			getSource().orElse(null), this.configuration);
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		MutableExtensionRegistry registry = context.getExtensionRegistry();
		List<Extension> additionalExtensions = invocationContext.getAdditionalExtensions();
		if (!additionalExtensions.isEmpty()) {
			MutableExtensionRegistry childRegistry = createRegistryFrom(registry, Stream.empty());
			additionalExtensions.forEach(extension -> childRegistry.registerExtension(extension, invocationContext));
			registry = childRegistry;
		}
		// TODO #871 Set a new ExtensionContext for each invocation to avoid the parent one from being closed
		return context.extend() //
				.withExtensionRegistry(registry) //
				.build();
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
