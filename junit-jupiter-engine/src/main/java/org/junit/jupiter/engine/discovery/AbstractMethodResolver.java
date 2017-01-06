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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
abstract class AbstractMethodResolver implements ElementResolver {

	private static final MethodFinder methodFinder = new MethodFinder();

	private final String segmentType;
	private final Predicate<Method> methodPredicate;

	AbstractMethodResolver(String segmentType, Predicate<Method> methodPredicate) {
		this.segmentType = segmentType;
		this.methodPredicate = methodPredicate;
	}

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (!(element instanceof Method))
			return Collections.emptySet();

		if (!(parent instanceof ClassTestDescriptor))
			return Collections.emptySet();

		Method method = (Method) element;
		if (!isRelevantMethod(method))
			return Collections.emptySet();

		return Collections.singleton(createTestDescriptor(parent, method));
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(this.segmentType))
			return Optional.empty();

		if (!(parent instanceof ClassTestDescriptor))
			return Optional.empty();

		Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
		if (!optionalMethod.isPresent())
			return Optional.empty();

		Method method = optionalMethod.get();
		if (!isRelevantMethod(method))
			return Optional.empty();

		return Optional.of(createTestDescriptor(parent, method));
	}

	private boolean isRelevantMethod(Method candidate) {
		return methodPredicate.test(candidate);
	}

	private UniqueId createUniqueId(Method method, TestDescriptor parent) {
		String methodId = String.format("%s(%s)", method.getName(),
			StringUtils.nullSafeToString(method.getParameterTypes()));
		return parent.getUniqueId().append(this.segmentType, methodId);
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
		return methodFinder.findMethod(segment.getValue(), parent.getTestClass());
	}

	private TestDescriptor createTestDescriptor(TestDescriptor parent, Method method) {
		UniqueId uniqueId = createUniqueId(method, parent);
		Class<?> testClass = ((ClassTestDescriptor) parent).getTestClass();
		return createTestDescriptor(uniqueId, testClass, method);
	}

	protected abstract TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method);

}
