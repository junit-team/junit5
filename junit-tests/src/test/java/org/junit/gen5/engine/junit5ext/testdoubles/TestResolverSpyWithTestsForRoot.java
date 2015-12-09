/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.testdoubles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public class TestResolverSpyWithTestsForRoot extends TestResolverSpy {
	private final MutableTestDescriptor root;
	private final MutableTestDescriptor resolvedTest;

	public TestResolverSpyWithTestsForRoot(MutableTestDescriptor root) {
		this.root = root;
		this.resolvedTest = new MutableTestDescriptorStub(root);
	}

	public MutableTestDescriptor getResolvedTest() {
		return resolvedTest;
	}

	@Override
	public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		super.resolveFor(parent, testPlanSpecification);

		if (root.equals(parent)) {
			return Arrays.asList(resolvedTest);
		}
		else {
			return Collections.emptyList();
		}
	}
}
