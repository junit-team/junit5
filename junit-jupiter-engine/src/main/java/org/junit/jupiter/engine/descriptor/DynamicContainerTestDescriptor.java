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
	private final boolean isEarlyDiscovery;

	DynamicContainerTestDescriptor(UniqueId uniqueId, int index, DynamicContainer dynamicContainer,
			TestSource testSource, DynamicDescendantFilter dynamicDescendantFilter, boolean isEarlyDiscovery) {
		super(uniqueId, index, dynamicContainer, testSource);
		this.dynamicContainer = dynamicContainer;
		this.testSource = testSource;
		this.dynamicDescendantFilter = dynamicDescendantFilter;
		this.isEarlyDiscovery = isEarlyDiscovery;
		
		if (isEarlyDiscovery) {
			discoverEarly();
		}
	}
	
	private void discoverEarly() {
		AtomicInteger index = new AtomicInteger(1);
		try (Stream<? extends DynamicNode> children = dynamicContainer.getChildren()) {
			children.forEach(child -> toDynamicDescriptor(index.getAndIncrement(), child, !isEarlyDiscovery));
		}
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		if (isEarlyDiscovery) {
			children.forEach(td -> dynamicTestExecutor.execute(td, false));
		} else {
			AtomicInteger index = new AtomicInteger(1);
			try (Stream<? extends DynamicNode> children = dynamicContainer.getChildren()) {
				// @formatter:off
				children.peek(child -> Preconditions.notNull(child, "individual dynamic node must not be null"))
						.map(child -> toDynamicDescriptor(index.getAndIncrement(), child, false))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.forEachOrdered(td -> dynamicTestExecutor.execute(td, true));
				// @formatter:on
			}

		}
		return context;
	}

	private Optional<JupiterTestDescriptor> toDynamicDescriptor(int index, DynamicNode childNode, boolean isEarlyDiscovery) {
		return createDynamicDescriptor(this, childNode, index, testSource, dynamicDescendantFilter, isEarlyDiscovery);
	}
}
