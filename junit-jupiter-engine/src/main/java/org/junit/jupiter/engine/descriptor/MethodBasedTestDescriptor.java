/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * Base class for {@link TestDescriptor TestDescriptors} based on Java methods.
 */
abstract class MethodBasedTestDescriptor extends JupiterTestDescriptor {

	private final Class<?> testClass;

	private final Method testMethod;

	/**
	 * Set of method-level tags; does not contain tags from parent.
	 */
	private final Set<TestTag> tags;

	MethodBasedTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		this(uniqueId, determineDisplayName(Preconditions.notNull(testMethod, "Method must not be null"),
			MethodBasedTestDescriptor::generateDefaultDisplayName), testClass, testMethod);
	}

	MethodBasedTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Method testMethod) {
		super(uniqueId, displayName, MethodSource.from(testClass, testMethod));

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = testMethod;
		this.tags = getTags(testMethod);
	}

	@Override
	public final Set<TestTag> getTags() {
		// return modifiable copy
		Set<TestTag> allTags = new LinkedHashSet<>(this.tags);
		getParent().ifPresent(parentDescriptor -> allTags.addAll(parentDescriptor.getTags()));
		return allTags;
	}

	@Override
	public Set<ExclusiveResource> getExclusiveResources() {
		return getExclusiveResourcesFromAnnotation(getTestMethod());
	}

	protected Optional<ExecutionMode> getExplicitExecutionMode() {
		return getExecutionModeFromAnnotation(getTestMethod());
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public String getLegacyReportingName() {
		return generateDefaultDisplayName(testMethod);
	}

	private static String generateDefaultDisplayName(Method testMethod) {
		return String.format("%s(%s)", testMethod.getName(),
			ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()));
	}

}
