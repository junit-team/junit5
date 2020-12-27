/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Represents a {@link TestIdentifier} from a {@link TestPlan} as a
 * {@link TestDescriptor} in an {@link ExecutionRequest}.
 *
 * @see EngineExecutionListenerAdaptor
 */
final class TestIdentifierAsTestDescriptor extends AbstractTestDescriptor {
	private final TestIdentifier testIdentifier;

	TestIdentifierAsTestDescriptor(UniqueId uniqueId, TestIdentifier testIdentifier) {
		super(uniqueId, testIdentifier.getDisplayName(), testIdentifier.getSource().orElse(null));
		this.testIdentifier = testIdentifier;
	}

	@Override
	public Type getType() {
		return testIdentifier.getType();
	}

	@Override
	public void removeFromHierarchy() {
		// Do not remove from hierarchy. The hierarchy is a view on the tests
		// in a TestPlan. Removing this node will not remove the test from
		// the plan. So when the test plan is executed this node must be
		// present.
	}

}
