/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;

public class NestedTestsResolver extends TestContainerResolver {

	public static final String SEGMENT_TYPE = "nested-class";

	@Override
	protected Class<? extends TestDescriptor> requiredParentType() {
		return ClassTestDescriptor.class;
	}

	@Override
	protected String getClassName(TestDescriptor parent, String segmentValue) {
		return ((ClassTestDescriptor) parent).getTestClass().getName() + "$" + segmentValue;
	}

	@Override
	protected String getSegmentType() {
		return SEGMENT_TYPE;
	}

	@Override
	protected String getSegmentValue(Class<?> testClass) {
		return testClass.getSimpleName();
	}

	@Override
	protected boolean isPotentialCandidate(Class<?> element) {
		return new IsNestedTestClass().test(element);
	}

	@Override
	protected TestDescriptor resolveClass(Class<?> testClass, UniqueId uniqueId) {
		return new NestedClassTestDescriptor(uniqueId, testClass);
	}
}
