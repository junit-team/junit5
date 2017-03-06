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
class DynamicTestTestDescriptor extends JupiterTestDescriptor {

	private final DynamicTest dynamicTest;

	public DynamicTestTestDescriptor(UniqueId uniqueId, DynamicTest dynamicTest, TestSource source) {
		super(uniqueId, dynamicTest.getDisplayName());
		this.dynamicTest = dynamicTest;
		setSource(source);
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		executeAndMaskThrowable(dynamicTest.getExecutable());
		return context;
	}
}
