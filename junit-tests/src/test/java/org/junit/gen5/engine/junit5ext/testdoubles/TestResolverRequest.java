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

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public class TestResolverRequest {
	public MutableTestDescriptor parent;
	public TestPlanSpecification testPlanSpecification;

	public TestResolverRequest(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		this.parent = parent;
		this.testPlanSpecification = testPlanSpecification;
	}
}
