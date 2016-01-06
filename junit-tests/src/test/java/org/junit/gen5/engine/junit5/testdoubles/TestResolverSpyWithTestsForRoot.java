/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;

public class TestResolverSpyWithTestsForRoot extends TestResolverSpy {
	private final TestResolverRegistry testResolverRegistry;
	private final TestDescriptor root;
	private final TestDescriptor resolvedTest;

	public TestResolverSpyWithTestsForRoot(TestResolverRegistry testResolverRegistry, TestDescriptor root) {
		this.testResolverRegistry = testResolverRegistry;
		this.root = root;
		this.resolvedTest = new TestDescriptorWithParentStub(root);
	}

	public TestDescriptor getResolvedTest() {
		return resolvedTest;
	}

	@Override
	public void resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		super.resolveFor(parent, testPlanSpecification);

		if (root.equals(parent)) {
			testResolverRegistry.notifyResolvers(resolvedTest, testPlanSpecification);
		}
	}
}
