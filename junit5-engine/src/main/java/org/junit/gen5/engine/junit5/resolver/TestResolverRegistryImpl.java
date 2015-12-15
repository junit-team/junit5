/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;

// TODO This class should become some kind of "JUnit" component, that will be initialized during start up
public class TestResolverRegistryImpl implements TestResolverRegistry {
	private TestEngine testEngine;
	private List<TestResolver> testResolvers;

	public TestResolverRegistryImpl(TestEngine testEngine) {
		this.testEngine = testEngine;
		this.testResolvers = new LinkedList<>();
	}

	@Override
	public void notifyResolvers(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		for (TestResolver testResolver : testResolvers) {
			testResolver.resolveFor(parent, testPlanSpecification);
		}
	}

	@Override
	public void notifyResolvers(List<TestDescriptor> parents, TestPlanSpecification testPlanSpecification) {
		for (TestDescriptor parent : parents) {
			notifyResolvers(parent, testPlanSpecification);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		testResolvers.add(testResolver);
		testResolver.setTestEngine(testEngine);
		testResolver.setTestResolverRegistry(this);
	}
}
