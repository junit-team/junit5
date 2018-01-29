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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestFactoryMethod;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@code TestFactoryMethodResolver} is an {@link ElementResolver} that is
 * able to resolve methods annotated with {@link TestFactory @TestFactory}.
 *
 * <p>It creates {@link TestFactoryTestDescriptor} instances.
 *
 * @since 5.0
 * @see ElementResolver
 * @see TestFactory
 * @see TestFactoryTestDescriptor
 */
class TestFactoryMethodResolver extends AbstractMethodResolver {

	private static final Predicate<Method> isTestFactoryMethod = new IsTestFactoryMethod();

	static final String SEGMENT_TYPE = "test-factory";

	TestFactoryMethodResolver() {
		super(SEGMENT_TYPE, isTestFactoryMethod);
	}

	@Override
	protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		return new TestFactoryTestDescriptor(uniqueId, testClass, method);
	}

}
