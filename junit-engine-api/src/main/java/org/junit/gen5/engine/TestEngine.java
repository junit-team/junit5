/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Collection;

import lombok.Value;

public interface TestEngine {

	default String getId() {
		return getClass().getCanonicalName();
	}

	Collection<TestDescriptor> discoverTests(TestPlanSpecification specification, TestDescriptor root);

	default boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor.getUniqueId().startsWith(getId());
	}

	default boolean supportsAll(Collection<TestDescriptor> testDescriptors) {
		return testDescriptors.stream().allMatch(testDescriptor -> supports(testDescriptor));
	}

	default TestDescriptor createEngineDescriptor() {
		return new EngineDescriptor(getId());
	}

	void execute(Collection<TestDescriptor> testDescriptions, TestExecutionListener testExecutionListener);

}

@Value
class EngineDescriptor implements TestDescriptor {

	private String engineId;


	public EngineDescriptor(String engineId) {
		this.engineId = engineId;
	}

	@Override
	public String getTestId() {
		return getEngineId();
	}

	@Override
	public String getDisplayName() {
		return getEngineId();
	}

	@Override
	public TestDescriptor getParent() {
		return null;
	}

	@Override
	public boolean isTest() {
		return false;
	}
}
