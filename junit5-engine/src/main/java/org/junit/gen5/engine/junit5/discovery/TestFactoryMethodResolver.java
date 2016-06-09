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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.gen5.api.TestFactory;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.TestFactoryTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.predicates.IsTestFactoryMethod;

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
class TestFactoryMethodResolver extends TestMethodResolver {

	private static final IsTestFactoryMethod isTestFactoryMethod = new IsTestFactoryMethod();

	static final String SEGMENT_TYPE = "test-factory";

	TestFactoryMethodResolver() {
		super(SEGMENT_TYPE);
	}

	@Override
	protected boolean isTestMethod(Method candidate) {
		return isTestFactoryMethod.test(candidate);
	}

	@Override
	protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new TestFactoryTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}

}
