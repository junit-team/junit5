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

import java.util.Optional;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicContainer}.
 *
 * @since 5.0
 */
class DynamicContainerTestDescriptor extends JupiterTestDescriptor {

	private final TestFactoryTestDescriptor root;
	private final DynamicContainer dynamicContainer;
	private final TestSource testSource;

	DynamicContainerTestDescriptor(TestFactoryTestDescriptor root, UniqueId uniqueId, DynamicContainer dynamicContainer,
			TestSource testSource) {
		super(uniqueId, dynamicContainer.getDisplayName());
		this.root = root;
		this.dynamicContainer = dynamicContainer;
		this.testSource = testSource;
		setSource(testSource);
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		int index = 1;
		for (DynamicNode childNode : dynamicContainer.getDynamicNodes()) {
			JupiterTestDescriptor childDescriptor = root.createDynamicDescriptor(this, childNode, index++, testSource);
			Optional<TestExecutionResult> optionalResult = dynamicTestExecutor.execute(childDescriptor);
			if (root.breaking(childNode, optionalResult)) {
				break;
			}
		}
		return context;
	}
}
