/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

/**
 * @since 1.3.1
 */
class NodeExecutionAdvisor {

	private final Map<TestDescriptor, ExecutionMode> forcedExecutionModeByTestDescriptor = new HashMap<>();
	private final Map<TestDescriptor, ResourceLock> resourceLocksByTestDescriptor = new HashMap<>();

	void forceExecutionMode(TestDescriptor testDescriptor, ExecutionMode executionMode) {
		forcedExecutionModeByTestDescriptor.put(testDescriptor, executionMode);
	}

	void useResourceLock(TestDescriptor testDescriptor, ResourceLock resourceLock) {
		resourceLocksByTestDescriptor.put(testDescriptor, resourceLock);
	}

	Optional<ExecutionMode> getForcedExecutionMode(TestDescriptor testDescriptor) {
		return Optional.ofNullable(forcedExecutionModeByTestDescriptor.get(testDescriptor));
	}

	ResourceLock getResourceLock(TestDescriptor testDescriptor) {
		return resourceLocksByTestDescriptor.getOrDefault(testDescriptor, NopLock.INSTANCE);
	}
}
