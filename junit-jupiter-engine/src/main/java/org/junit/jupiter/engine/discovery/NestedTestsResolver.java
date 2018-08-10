/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsNestedTestClass;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
class NestedTestsResolver extends TestContainerResolver {

	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	static final String SEGMENT_TYPE = "nested-class";

	public NestedTestsResolver(ConfigurationParameters configurationParameters) {
		super(configurationParameters);
	}

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
		return isNestedTestClass.test(element);
	}

	@Override
	protected TestDescriptor resolveClass(Class<?> testClass, UniqueId uniqueId) {
		return new NestedClassTestDescriptor(uniqueId, testClass, this.configurationParameters);
	}

}
