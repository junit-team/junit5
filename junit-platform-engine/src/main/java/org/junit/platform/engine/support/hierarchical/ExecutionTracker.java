/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.HashSet;
import java.util.Set;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.0
 */
class ExecutionTracker {

	private final Set<UniqueId> executedUniqueIds = new HashSet<>();

	void markExecuted(TestDescriptor testDescriptor) {
		executedUniqueIds.add(testDescriptor.getUniqueId());
	}

	boolean wasAlreadyExecuted(TestDescriptor testDescriptor) {
		return executedUniqueIds.contains(testDescriptor.getUniqueId());
	}
}
