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

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestTemplateMethod;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * {@code TestTemplateMethodResolver} is an {@link ElementResolver} that is
 * able to resolve methods annotated with {@link TestTemplate @TestTemplate}.
 *
 * <p>It creates {@link TestTemplateTestDescriptor} instances.
 *
 * @since 5.0
 * @see ElementResolver
 * @see TestTemplate
 * @see TestTemplateTestDescriptor
 */
class TestTemplateMethodResolver extends AbstractMethodResolver {

	private static final Predicate<Method> isTestTemplateMethod = new IsTestTemplateMethod();

	static final String SEGMENT_TYPE = "test-template";

	TestTemplateMethodResolver() {
		super(SEGMENT_TYPE, isTestTemplateMethod);
	}

	@Override
	protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method) {
		return new TestTemplateTestDescriptor(uniqueId, testClass, method);
	}

}
