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

import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.createDynamicDescriptor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicContainer}.
 *
 * @since 5.0
 */
class DynamicContainerTestDescriptor extends DynamicNodeTestDescriptor {

	private final DynamicContainer dynamicContainer;
	private final TestSource testSource;
	private final DynamicDescendantFilter dynamicDescendantFilter;

	DynamicContainerTestDescriptor(UniqueId uniqueId, int index, DynamicContainer dynamicContainer,
			TestSource testSource, DynamicDescendantFilter dynamicDescendantFilter) {
		super(uniqueId, index, dynamicContainer, testSource);
		this.dynamicContainer = dynamicContainer;
		this.testSource = testSource;
		this.dynamicDescendantFilter = dynamicDescendantFilter;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}
	
	public void disvoverTests(DynamicTestExecutor dynamicTestExecutor) {
		AtomicInteger index = new AtomicInteger(1);
		try (Stream<? extends DynamicNode> children = dynamicContainer.getChildren()) {
			// @formatter:off
			children.peek(child -> Preconditions.notNull(child, "individual dynamic node must not be null"))
					.forEach(child -> toDynamicDescriptor(index.getAndIncrement(), child, dynamicTestExecutor));
			// @formatter:on
		}
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) {
		children.forEach(dynamicTestExecutor::execute);
		return context;
	}

	private Optional<JupiterTestDescriptor> toDynamicDescriptor(int index, DynamicNode childNode, DynamicTestExecutor dynamicTestExecutor) {
		Optional<JupiterTestDescriptor> descriptor = createDynamicDescriptor(getUniqueId(), childNode, index, testSource, dynamicDescendantFilter);
		descriptor.ifPresent(td -> {
			addChild(td);
			dynamicTestExecutor.dynamicRegister(td);
			if (td instanceof DynamicContainerTestDescriptor) {
				((DynamicContainerTestDescriptor) td).disvoverTests(dynamicTestExecutor);
			}

		});
		
		return descriptor;
	}
}
