/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Base class for {@link TestDescriptor TestDescriptors} based on Java methods.
 */
abstract class MethodBasedTestDescriptor extends JupiterTestDescriptor {

	private final Class<?> testClass;
	private final Method testMethod;

	MethodBasedTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		this(uniqueId, determineDisplayName(Preconditions.notNull(testMethod, "Method must not be null"),
			MethodBasedTestDescriptor::generateDefaultDisplayName), testClass, testMethod);
	}

	MethodBasedTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Method testMethod) {
		super(uniqueId, displayName);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = Preconditions.notNull(testMethod, "Method must not be null");

		setSource(new MethodSource(testMethod));
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	private static String generateDefaultDisplayName(Method testMethod) {
		return String.format("%s(%s)", testMethod.getName(),
			StringUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()));
	}

}
