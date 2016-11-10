/*
 * Copyright 2015-2016 the original author or authors.
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

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
class TestMethodResolver implements ElementResolver {

	private static final IsTestMethod isTestMethod = new IsTestMethod();
	private static final MethodFinder methodFinder = new MethodFinder();

	static final String SEGMENT_TYPE = "method";

	private final String segmentType;

	TestMethodResolver() {
		this(SEGMENT_TYPE);
	}

	TestMethodResolver(String segmentType) {
		this.segmentType = segmentType;
	}

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
		if (!segment.getType().equals(this.segmentType))
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

	protected boolean isTestMethod(Method candidate) {
		return isTestMethod.test(candidate);
	}

	private UniqueId createUniqueId(Method testMethod, TestDescriptor parent) {
		String methodId = String.format("%s(%s)", testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
		return parent.getUniqueId().append(this.segmentType, methodId);
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
		return methodFinder.findMethod(segment.getValue(), parent.getTestClass());
	}

	protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new MethodTestDescriptor(uniqueId, parentClassDescriptor, testMethod);
	}

}
