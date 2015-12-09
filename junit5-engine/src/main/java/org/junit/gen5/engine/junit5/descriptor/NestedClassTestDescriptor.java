/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.Optional;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.execution.AfterEachCallback;
import org.junit.gen5.engine.junit5.execution.BeforeEachCallback;
import org.junit.gen5.engine.junit5.execution.JUnit5Context;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;

/**
 * {@link TestDescriptor} for tests based on nested (but not static) Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name of parent}@{simple class name}</code>.
 *
 * @since 5.0
 */
public class NestedClassTestDescriptor extends ClassTestDescriptor {

	NestedClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId, testClass);
	}

	@Override
	protected TestInstanceProvider testInstanceProvider(JUnit5Context context) {
		return () -> {
			Object outerInstance = context.getTestInstanceProvider().getTestInstance();
			return ReflectionUtils.newInstance(getTestClass(), outerInstance);
		};
	}

	@Override
	protected BeforeEachCallback beforeEachCallback(JUnit5Context context) {
		return (testExtensionContext, testInstance) -> {
			Optional<Object> outerInstance = ReflectionUtils.getOuterInstance(testInstance);
			if (outerInstance.isPresent()) {
				context.getBeforeEachCallback().beforeEach(testExtensionContext, outerInstance.get());
			}
			super.beforeEachCallback(context).beforeEach(testExtensionContext, testInstance);
		};
	}

	@Override
	protected AfterEachCallback afterEachCallback(JUnit5Context context) {
		return (testExtensionContext, testInstance, throwable) -> {
			super.afterEachCallback(context).afterEach(testExtensionContext, testInstance, throwable);
			Optional<Object> outerInstance = ReflectionUtils.getOuterInstance(testInstance);
			if (outerInstance.isPresent()) {
				context.getAfterEachCallback().afterEach(testExtensionContext, outerInstance.get(), throwable);
			}
		};
	}

}
