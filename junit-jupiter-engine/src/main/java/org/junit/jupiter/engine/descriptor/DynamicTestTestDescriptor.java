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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link TestDescriptor} for a {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestTestDescriptor extends DynamicNodeTestDescriptor {

	private final DynamicTest dynamicTest;

	DynamicTestTestDescriptor(UniqueId uniqueId, int index, DynamicTest dynamicTest, TestSource source) {
		super(uniqueId, index, dynamicTest, source);
		this.dynamicTest = dynamicTest;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		executeAndMaskThrowable(dynamicTest.getExecutable());
		return context;
	}

}
