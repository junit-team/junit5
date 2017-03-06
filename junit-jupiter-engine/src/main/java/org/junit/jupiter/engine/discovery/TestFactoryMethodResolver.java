/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestFactoryMethod;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@code TestFactoryMethodResolver} is an {@link ElementResolver}
 * that is able to resolve test factory methods annotated with
 * {@link TestFactory @TestFactory}.
 *
 * <p>It will create {@link TestFactoryTestDescriptor} instances.
 *
 * @since 5.0
 * @see ElementResolver
 * @see TestFactory
 * @see TestFactoryTestDescriptor
 */
@API(Experimental)
class TestFactoryMethodResolver extends AbstractMethodResolver {

	static final String SEGMENT_TYPE = "test-factory";

	TestFactoryMethodResolver() {
		super(SEGMENT_TYPE, new IsTestFactoryMethod());
	}

	@Override
	protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		return new TestFactoryTestDescriptor(uniqueId, testClass, method);
	}

}
