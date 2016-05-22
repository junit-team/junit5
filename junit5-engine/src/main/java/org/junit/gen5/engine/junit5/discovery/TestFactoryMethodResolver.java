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

import org.junit.gen5.api.TestFactory;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.TestFactoryTestDescriptor;

/**
 * {@code TestFactoryMethodResolver} is a special {@code ElementResolver}
 * which is able to resolve test factory methods denoted by {@code @TestFactory}.
 *
 * <p>It will create {@code TestFactoryTestDescriptor} instances.
 *
 * @since 5.0
 * @see ElementResolver
 * @see TestFactory
 * @see TestFactoryTestDescriptor
 */
public class TestFactoryMethodResolver extends TestMethodResolver {

	public static final String SEGMENT_TYPE = "test-factory";

	protected boolean isTestMethod(Method candidate) {
		return new IsTestFactoryMethod().test(candidate);
	}

	protected UniqueId createUniqueId(Method testMethod, TestDescriptor parent) {
		String methodId = String.format("%s(%s)", testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
		return parent.getUniqueId().append(SEGMENT_TYPE, methodId);
	}

	protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new TestFactoryTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}

}
