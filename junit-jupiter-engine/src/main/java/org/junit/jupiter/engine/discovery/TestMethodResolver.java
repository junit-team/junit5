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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@code TestMethodResolver} is an {@link ElementResolver} that is able to
 * resolve methods annotated with {@link Test @Test}.
 *
 * <p>It creates {@link TestMethodTestDescriptor} instances.
 *
 * @since 5.0
 * @see ElementResolver
 * @see Test
 * @see TestMethodTestDescriptor
 */
class TestMethodResolver extends AbstractMethodResolver {

	private static final Predicate<Method> isTestMethod = new IsTestMethod();

	static final String SEGMENT_TYPE = "method";

	TestMethodResolver() {
		super(SEGMENT_TYPE, isTestMethod);
	}

	@Override
	protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		return new TestMethodTestDescriptor(uniqueId, testClass, method);
	}

}
