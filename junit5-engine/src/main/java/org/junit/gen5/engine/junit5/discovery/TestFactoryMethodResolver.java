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

import java.lang.reflect.Method;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.TestFactoryTestDescriptor;

public class TestFactoryMethodResolver extends TestMethodResolver {

	public static final String SEGMENT_TYPE = "test-factory";

	private final IsTestFactoryMethod isTestFactoryMethod = new IsTestFactoryMethod();

	public TestFactoryMethodResolver() {
		super(SEGMENT_TYPE);
	}

	protected boolean isTestMethod(Method candidate) {
		return isTestFactoryMethod.test(candidate);
	}

	protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new TestFactoryTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}

}
