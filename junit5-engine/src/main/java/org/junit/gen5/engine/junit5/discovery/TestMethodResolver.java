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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class TestMethodResolver implements ElementResolver {

	public static final String SEGMENT_TYPE = "method";

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Method))
			return Collections.emptySet();

		if (!(parent instanceof ClassTestDescriptor))
			return Collections.emptySet();

		Method testMethod = (Method) element;
		if (!isTestMethod(testMethod))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueId(testMethod, parent);
		return Collections.singleton(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(SEGMENT_TYPE))
			return Optional.empty();

		if (!(parent instanceof ClassTestDescriptor))
			return Optional.empty();

		Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
		if (!optionalMethod.isPresent())
			return Optional.empty();

		Method testMethod = optionalMethod.get();
		if (!isTestMethod(testMethod))
			return Optional.empty();

		UniqueId uniqueId = createUniqueId(testMethod, parent);
		return Optional.of(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
	}

	private boolean isTestMethod(Method candidate) {
		return new IsTestMethod().test(candidate);
	}

	private UniqueId createUniqueId(Method testMethod, TestDescriptor parent) {
		String methodId = String.format("%s(%s)", testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
		return parent.getUniqueId().append(SEGMENT_TYPE, methodId);
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
		return new MethodFinder().findMethod(segment.getValue(), parent.getTestClass());
	}

	private TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new MethodTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}
}
